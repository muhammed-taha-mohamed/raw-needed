package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.complaint.ComplaintMessageRequestDto;
import com.rawneeded.dto.complaint.ComplaintMessageResponseDto;
import com.rawneeded.dto.complaint.ComplaintResponseDto;
import com.rawneeded.dto.complaint.CreateComplaintRequestDto;
import com.rawneeded.enumeration.ComplaintStatus;
import com.rawneeded.service.IComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("api/v1/complaints")
public class ComplaintController {

    private final IComplaintService complaintService;

    @PostMapping
    @Operation(
            summary = "Create a new complaint",
            description = "User creates a complaint"
    )
    public ResponseEntity<ResponsePayload> createComplaint(
            @Valid @RequestBody CreateComplaintRequestDto request) {
        ComplaintResponseDto complaint = complaintService.createComplaint(request);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", complaint,
                        "message", "Complaint created successfully"
                ))
                .build());
    }

    @GetMapping("/{complaintId}")
    @Operation(
            summary = "Get complaint by ID",
            description = "Get a specific complaint with all its messages (user can see own complaints, admin can see all)"
    )
    public ResponseEntity<ResponsePayload> getComplaintById(
            @PathVariable String complaintId) {
        ComplaintResponseDto complaint = complaintService.getComplaintById(complaintId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", complaint
                ))
                .build());
    }

    @GetMapping("/my-complaints")
    @Operation(
            summary = "Get my complaints",
            description = "Get all complaints created by the current user"
    )
    public ResponseEntity<ResponsePayload> getMyComplaints(Pageable pageable) {
        Page<ComplaintResponseDto> complaints = complaintService.getMyComplaints(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", complaints
                ))
                .build());
    }

    @GetMapping("/admin/all")
    @Operation(
            summary = "Get all complaints (Admin only)",
            description = "Get all complaints (admin only)"
    )
    public ResponseEntity<ResponsePayload> getAllComplaints(Pageable pageable) {
        Page<ComplaintResponseDto> complaints = complaintService.getAllComplaints(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", complaints
                ))
                .build());
    }

    @GetMapping("/admin/status/{status}")
    @Operation(
            summary = "Get complaints by status (Admin only)",
            description = "Get complaints filtered by status (OPEN or CLOSED)"
    )
    public ResponseEntity<ResponsePayload> getComplaintsByStatus(
            @PathVariable ComplaintStatus status,
            Pageable pageable) {
        Page<ComplaintResponseDto> complaints = complaintService.getComplaintsByStatus(status, pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", complaints
                ))
                .build());
    }

    @PostMapping("/{complaintId}/messages")
    @Operation(
            summary = "Add a message to a complaint",
            description = "User or admin adds a message/reply to a complaint"
    )
    public ResponseEntity<ResponsePayload> addMessage(
            @PathVariable String complaintId,
            @Valid @RequestBody ComplaintMessageRequestDto request) {
        ComplaintMessageResponseDto message = complaintService.addMessage(complaintId, request);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", message,
                        "message", "Message added successfully"
                ))
                .build());
    }

    @PutMapping("/{complaintId}/close")
    @Operation(
            summary = "Close a complaint",
            description = "User or admin closes a complaint"
    )
    public ResponseEntity<ResponsePayload> closeComplaint(
            @PathVariable String complaintId) {
        ComplaintResponseDto complaint = complaintService.closeComplaint(complaintId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", complaint,
                        "message", "Complaint closed successfully"
                ))
                .build());
    }
}
