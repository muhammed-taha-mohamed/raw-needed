package com.rawneeded.service.impl;

import com.rawneeded.dto.MailDto;
import com.rawneeded.dto.complaint.ComplaintMessageRequestDto;
import com.rawneeded.dto.complaint.ComplaintMessageResponseDto;
import com.rawneeded.dto.complaint.ComplaintResponseDto;
import com.rawneeded.dto.complaint.CreateComplaintRequestDto;
import com.rawneeded.enumeration.TemplateName;
import com.rawneeded.enumeration.ComplaintStatus;
import com.rawneeded.enumeration.Role;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.model.Complaint;
import com.rawneeded.model.ComplaintMessage;
import com.rawneeded.model.User;
import com.rawneeded.enumeration.NotificationType;
import com.rawneeded.repository.ComplaintMessageRepository;
import com.rawneeded.repository.ComplaintRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.IComplaintService;
import com.rawneeded.service.INotificationService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ComplaintServiceImpl implements IComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintMessageRepository complaintMessageRepository;
    private final UserRepository userRepository;
    private final MessagesUtil messagesUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private final INotificationService notificationService;
    private final NotificationService emailService;

    @Override
    public ComplaintResponseDto createComplaint(CreateComplaintRequestDto requestDto) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getOwnerIdFromToken(token);
            log.info("Creating complaint for user: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            Complaint complaint = Complaint.builder()
                    .user(user)
                    .userId(userId)
                    .subject(requestDto.getSubject())
                    .description(requestDto.getDescription())
                    .image(requestDto.getImage())
                    .status(ComplaintStatus.OPEN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            complaint = complaintRepository.save(complaint);
            log.info("Complaint created successfully with id: {}", complaint.getId());

            // Create initial message from the complaint description
            ComplaintMessage initialMessage = ComplaintMessage.builder()
                    .complaint(complaint)
                    .complaintId(complaint.getId())
                    .user(user)
                    .userId(userId)
                    .message(requestDto.getDescription())
                    .image(requestDto.getImage())
                    .isAdmin(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            complaintMessageRepository.save(initialMessage);

            // Auto support reply from a SUPER_ADMIN user (frontend shows localized text for key COMPLAINT_AUTO_REPLY)
            List<User> superAdmins = userRepository.findAllByRole(Role.SUPER_ADMIN);
            User autoReplySender = superAdmins.isEmpty() ? null : superAdmins.get(0);
            ComplaintMessage autoReply = ComplaintMessage.builder()
                    .complaint(complaint)
                    .complaintId(complaint.getId())
                    .user(autoReplySender)
                    .userId(autoReplySender != null ? autoReplySender.getId() : null)
                    .message("COMPLAINT_AUTO_REPLY")
                    .image(null)
                    .isAdmin(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            complaintMessageRepository.save(autoReply);

            // Send notification and email to all admins
            sendNotificationToAdmins(complaint.getId(), complaint.getSubject(), complaint.getDescription(), user.getName());

            return mapToResponseDto(complaint);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating complaint: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("COMPLAINT_CREATE_FAIL"));
        }
    }

    @Override
    public ComplaintResponseDto getComplaintById(String complaintId) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getOwnerIdFromToken(token);
            Role role = jwtTokenProvider.getRoleFromToken(token);
            
            log.info("Fetching complaint: {} for user: {}", complaintId, userId);

            Complaint complaint = complaintRepository.findById(complaintId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("COMPLAINT_NOT_FOUND")));

            // Check if user is admin or the complaint owner
            boolean isAdmin = role == Role.SUPER_ADMIN;
            if (!isAdmin && !complaint.getUserId().equals(userId)) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED_COMPLAINT_ACCESS"));
            }

            return mapToResponseDto(complaint);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching complaint: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("COMPLAINT_FETCH_FAIL"));
        }
    }

    @Override
    public Page<ComplaintResponseDto> getMyComplaints(Pageable pageable) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getOwnerIdFromToken(token);
            log.info("Fetching complaints for user: {}", userId);

            Page<Complaint> complaints = complaintRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            return complaints.map(this::mapToResponseDto);
        } catch (Exception e) {
            log.error("Error fetching user complaints: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("COMPLAINT_FETCH_MY_FAIL"));
        }
    }

    @Override
    public Page<ComplaintResponseDto> getAllComplaints(Pageable pageable) {
        try {
            String token = messagesUtil.getAuthToken();
            Role role = jwtTokenProvider.getRoleFromToken(token);
            
            if (role != Role.SUPER_ADMIN) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED_ACCESS"));
            }

            log.info("Fetching all complaints");
            Page<Complaint> complaints = complaintRepository.findAllByOrderByCreatedAtDesc(pageable);
            return complaints.map(this::mapToResponseDto);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching all complaints: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("COMPLAINT_FETCH_ALL_FAIL"));
        }
    }

    @Override
    public Page<ComplaintResponseDto> getComplaintsByStatus(ComplaintStatus status, Pageable pageable) {
        try {
            String token = messagesUtil.getAuthToken();
            Role role = jwtTokenProvider.getRoleFromToken(token);
            
            if (role != Role.SUPER_ADMIN) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED_ACCESS"));
            }

            log.info("Fetching complaints by status: {}", status);
            Page<Complaint> complaints = complaintRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
            return complaints.map(this::mapToResponseDto);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching complaints by status: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("COMPLAINT_FETCH_BY_STATUS_FAIL"));
        }
    }

    @Override
    public ComplaintMessageResponseDto addMessage(String complaintId, ComplaintMessageRequestDto requestDto) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getOwnerIdFromToken(token);
            Role role = jwtTokenProvider.getRoleFromToken(token);
            
            log.info("Adding message to complaint: {} from user: {}", complaintId, userId);

            Complaint complaint = complaintRepository.findById(complaintId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("COMPLAINT_NOT_FOUND")));

            // Check if complaint is closed
            if (complaint.getStatus() == ComplaintStatus.CLOSED) {
                throw new AbstractException(messagesUtil.getMessage("COMPLAINT_ALREADY_CLOSED"));
            }

            // Check authorization
            boolean isAdmin = role == Role.SUPER_ADMIN;
            if (!isAdmin && !complaint.getUserId().equals(userId)) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED_COMPLAINT_ACCESS"));
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            ComplaintMessage message = ComplaintMessage.builder()
                    .complaint(complaint)
                    .complaintId(complaintId)
                    .user(user)
                    .userId(userId)
                    .message(requestDto.getMessage())
                    .image(requestDto.getImage())
                    .isAdmin(isAdmin)
                    .createdAt(LocalDateTime.now())
                    .build();

            message = complaintMessageRepository.save(message);

            // Update complaint updatedAt
            complaint.setUpdatedAt(LocalDateTime.now());
            complaintRepository.save(complaint);

            // Send notifications and emails
            if (isAdmin) {
                notificationService.sendNotificationToUser(
                        complaint.getUserId(),
                        NotificationType.GENERAL,
                        "NOTIFICATION_COMPLAINT_ADMIN_REPLY_TITLE",
                        "NOTIFICATION_COMPLAINT_ADMIN_REPLY_MESSAGE",
                        complaintId,
                        "COMPLAINT",
                        complaint.getSubject()
                );
                User owner = userRepository.findById(complaint.getUserId()).orElse(null);
                if (owner != null && owner.getEmail() != null && !owner.getEmail().isEmpty()) {
                    try {
                        emailService.sendEmail(MailDto.builder()
                                .toEmail(owner.getEmail())
                                .subject(messagesUtil.getMessage("EMAIL_SUBJECT_COMPLAINT_REPLY"))
                                .templateName(TemplateName.COMPLAINT_REPLY_USER)
                                .model(Map.of(
                                        "userName", owner.getName() != null ? owner.getName() : "",
                                        "subject", complaint.getSubject() != null ? complaint.getSubject() : ""
                                ))
                                .build());
                    } catch (Exception e) {
                        log.error("Failed to send complaint-reply email: {}", e.getMessage());
                    }
                }
            } else {
                sendNotificationToAdmins(complaintId, complaint.getSubject(), complaint.getDescription(), user.getName());
            }

            log.info("Message added successfully");
            return mapMessageToResponseDto(message);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding message: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("COMPLAINT_MESSAGE_ADD_FAIL"));
        }
    }

    @Override
    public ComplaintResponseDto closeComplaint(String complaintId) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = jwtTokenProvider.getOwnerIdFromToken(token);
            Role role = jwtTokenProvider.getRoleFromToken(token);
            
            log.info("Closing complaint: {} by user: {}", complaintId, userId);

            Complaint complaint = complaintRepository.findById(complaintId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("COMPLAINT_NOT_FOUND")));

            // Check if already closed
            if (complaint.getStatus() == ComplaintStatus.CLOSED) {
                throw new AbstractException(messagesUtil.getMessage("COMPLAINT_ALREADY_CLOSED"));
            }

            // Check authorization - only admin or complaint owner can close
            boolean isAdmin = role == Role.SUPER_ADMIN;
            if (!isAdmin && !complaint.getUserId().equals(userId)) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED_COMPLAINT_ACCESS"));
            }

            complaint.setStatus(ComplaintStatus.CLOSED);
            complaint.setClosedAt(LocalDateTime.now());
            complaint.setUpdatedAt(LocalDateTime.now());
            complaint = complaintRepository.save(complaint);

            // Send notification and email to complaint owner if closed by admin
            if (isAdmin && !complaint.getUserId().equals(userId)) {
                notificationService.sendNotificationToUser(
                        complaint.getUserId(),
                        NotificationType.GENERAL,
                        "NOTIFICATION_COMPLAINT_CLOSED_TITLE",
                        "NOTIFICATION_COMPLAINT_CLOSED_MESSAGE",
                        complaintId,
                        "COMPLAINT",
                        complaint.getSubject()
                );
                User owner = userRepository.findById(complaint.getUserId()).orElse(null);
                if (owner != null && owner.getEmail() != null && !owner.getEmail().isEmpty()) {
                    try {
                        emailService.sendEmail(MailDto.builder()
                                .toEmail(owner.getEmail())
                                .subject(messagesUtil.getMessage("EMAIL_SUBJECT_COMPLAINT_CLOSED"))
                                .templateName(TemplateName.COMPLAINT_CLOSED_USER)
                                .model(Map.of(
                                        "userName", owner.getName() != null ? owner.getName() : "",
                                        "subject", complaint.getSubject() != null ? complaint.getSubject() : ""
                                ))
                                .build());
                    } catch (Exception e) {
                        log.error("Failed to send complaint-closed email: {}", e.getMessage());
                    }
                }
            }

            log.info("Complaint closed successfully");
            return mapToResponseDto(complaint);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error closing complaint: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("COMPLAINT_CLOSE_FAIL"));
        }
    }

    private ComplaintResponseDto mapToResponseDto(Complaint complaint) {
        List<ComplaintMessage> messages = complaintMessageRepository.findByComplaintIdOrderByCreatedAtAsc(complaint.getId());
        List<ComplaintMessageResponseDto> messageDtos = messages.stream()
                .map(this::mapMessageToResponseDto)
                .collect(Collectors.toList());

        return ComplaintResponseDto.builder()
                .id(complaint.getId())
                .userId(complaint.getUserId())
                .userName(complaint.getUser() != null ? complaint.getUser().getName() : null)
                .subject(complaint.getSubject())
                .description(complaint.getDescription())
                .image(complaint.getImage())
                .status(complaint.getStatus())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .closedAt(complaint.getClosedAt())
                .messages(messageDtos)
                .build();
    }

    private ComplaintMessageResponseDto mapMessageToResponseDto(ComplaintMessage message) {
        return ComplaintMessageResponseDto.builder()
                .id(message.getId())
                .complaintId(message.getComplaintId())
                .userId(message.getUserId())
                .userName(message.getUser() != null ? message.getUser().getName() : null)
                .message(message.getMessage())
                .image(message.getImage())
                .isAdmin(message.isAdmin())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private void sendNotificationToAdmins(String complaintId, String subject, String description, String userName) {
        try {
            List<User> admins = userRepository.findAllByRole(Role.SUPER_ADMIN);
            for (User admin : admins) {
                notificationService.sendNotificationToUser(
                        admin.getId(),
                        NotificationType.GENERAL,
                        "NOTIFICATION_COMPLAINT_CREATED_TITLE",
                        "NOTIFICATION_COMPLAINT_CREATED_MESSAGE",
                        complaintId,
                        "COMPLAINT",
                        userName,
                        subject
                );
                if (admin.getEmail() != null && !admin.getEmail().isEmpty()) {
                    try {
                        emailService.sendEmail(MailDto.builder()
                                .toEmail(admin.getEmail())
                                .subject(messagesUtil.getMessage("EMAIL_SUBJECT_COMPLAINT_CREATED"))
                                .templateName(TemplateName.COMPLAINT_CREATED_ADMIN)
                                .model(Map.of(
                                        "userName", userName != null ? userName : "",
                                        "subject", subject != null ? subject : "",
                                        "description", description != null ? description : ""
                                ))
                                .build());
                    } catch (Exception e) {
                        log.error("Failed to send complaint-created email to admin: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error sending notifications/emails to admins: {}", e.getMessage());
        }
    }
}
