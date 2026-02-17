package com.rawneeded.service.impl;

import com.rawneeded.dto.post.CreateOfferRequest;
import com.rawneeded.dto.private_order.CreatePrivateOrderRequest;
import com.rawneeded.dto.post.OfferResponseDto;
import com.rawneeded.dto.private_order.PrivateOrderResponseDto;
import com.rawneeded.dto.post.RespondToOfferRequest;
import com.rawneeded.enumeration.PostStatus;
import com.rawneeded.enumeration.PostType;
import com.rawneeded.enumeration.PrivateOrderTargetType;
import com.rawneeded.enumeration.Role;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.model.Offer;
import com.rawneeded.model.Post;
import com.rawneeded.model.User;
import com.rawneeded.repository.OfferRepository;
import com.rawneeded.repository.PostRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.service.IPrivateOrderService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class PrivateOrderServiceImpl implements IPrivateOrderService {

    private final PostRepository postRepository;
    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final MessagesUtil messagesUtil;

    @Override
    public PrivateOrderResponseDto createPrivateOrder(CreatePrivateOrderRequest request) {
        try {
            log.info("Creating new private order: {}", request.getMaterialName());

            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getOwnerIdFromToken(token);

            User creator = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            Post post = Post.builder()
                    .materialName(request.getMaterialName())
                    .image(request.getImage())
                    .quantity(request.getQuantity())
                    .unit(request.getUnit())
                    .postType(mapTargetType(request.getTargetType()))
                    .createdBy(creator)
                    .createdById(creator.getId())
                    .createdByName(creator.getName())
                    .createdByOrganizationName(creator.getOrganizationName())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .active(true)
                    .status(PostStatus.OPEN)
                    .build();

            post = postRepository.save(post);
            log.info("Private order created successfully with id: {}", post.getId());

            return toPrivateOrderResponseDto(post);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating private order: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("POST_CREATE_FAIL"));
        }
    }

    @Override
    public Page<PrivateOrderResponseDto> getAllPrivateOrders(Pageable pageable) {
        try {
            log.info("Fetching all private orders");
            String token = messagesUtil.getAuthToken();
            Role role = tokenProvider.getRoleFromToken(token);
            String ownerId = tokenProvider.getOwnerIdFromToken(token);

            PostType type = (role == Role.CUSTOMER_OWNER || role == Role.CUSTOMER_STAFF)
                    ? PostType.CUSTOMERS
                    : PostType.SUPPLIERS;

            // Backend-level exclusion of current user's own private orders from "All"
            Page<Post> posts = postRepository.findByPostTypeAndActiveTrueAndCreatedByIdNotOrderByCreatedAtDesc(type, ownerId, pageable);
            return posts.map(this::toPrivateOrderResponseDto);
        } catch (Exception e) {
            log.error("Error fetching all private orders: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("POST_FETCH_ALL_FAIL"));
        }
    }

    @Override
    public PrivateOrderResponseDto getPrivateOrderById(String privateOrderId) {
        try {
            log.info("Fetching private order by id: {}", privateOrderId);
            Post post = postRepository.findById(privateOrderId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("POST_NOT_FOUND")));
            return toPrivateOrderResponseDto(post);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching private order: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("POST_FETCH_ONE_FAIL"));
        }
    }

    @Override
    public OfferResponseDto createOffer(String privateOrderId, CreateOfferRequest request) {
        try {
            log.info("Creating offer for private order: {}", privateOrderId);

            Post post = postRepository.findById(privateOrderId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("POST_NOT_FOUND")));

            if (!post.isActive()) {
                throw new AbstractException(messagesUtil.getMessage("POST_NOT_ACTIVE"));
            }

            if (post.getStatus() != PostStatus.OPEN) {
                throw new AbstractException(messagesUtil.getMessage("POST_NOT_OPEN"));
            }

            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getOwnerIdFromToken(token);

            User offerer = userRepository.findById(userId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("USER_NOT_FOUND")));

            List<Offer> existingOffers = offerRepository.findByPostId(privateOrderId);
            boolean alreadyOffered = existingOffers.stream()
                    .anyMatch(offer -> offer.getOfferedById().equals(userId));

            if (alreadyOffered) {
                throw new AbstractException(messagesUtil.getMessage("OFFER_ALREADY_EXISTS"));
            }

            Offer offer = Offer.builder()
                    .postId(privateOrderId)
                    .offeredBy(offerer)
                    .offeredById(offerer.getId())
                    .offeredByName(offerer.getName())
                    .offeredByOrganizationName(offerer.getOrganizationName())
                    .price(request.getPrice())
                    .availableQuantity(request.getAvailableQuantity())
                    .shippingInfo(request.getShippingInfo())
                    .estimatedDelivery(request.getEstimatedDelivery())
                    .notes(request.getNotes())
                    .accepted(null)
                    .createdAt(LocalDateTime.now())
                    .build();

            offer = offerRepository.save(offer);
            return toOfferResponseDto(offer);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating offer: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("OFFER_CREATE_FAIL"));
        }
    }

    @Override
    public OfferResponseDto respondToOffer(String privateOrderId, String offerId, RespondToOfferRequest request) {
        try {
            log.info("Responding to offer: {} for private order: {}", offerId, privateOrderId);

            Post post = postRepository.findById(privateOrderId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("POST_NOT_FOUND")));

            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getOwnerIdFromToken(token);

            if (!post.getCreatedById().equals(userId)) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED_POST_ACCESS"));
            }

            Offer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("OFFER_NOT_FOUND")));

            if (!offer.getPostId().equals(privateOrderId)) {
                throw new AbstractException(messagesUtil.getMessage("OFFER_POST_MISMATCH"));
            }

            if (offer.getAccepted() != null) {
                throw new AbstractException(messagesUtil.getMessage("OFFER_ALREADY_RESPONDED"));
            }

            offer.setAccepted(request.getAccepted());
            offer.setResponseMessage(request.getResponseMessage());
            offer.setRespondedAt(LocalDateTime.now());

            offer = offerRepository.save(offer);
            return toOfferResponseDto(offer);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error responding to offer: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("OFFER_RESPOND_FAIL"));
        }
    }

    @Override
    public PrivateOrderResponseDto closePrivateOrder(String privateOrderId) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getOwnerIdFromToken(token);

            Post post = postRepository.findById(privateOrderId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("POST_NOT_FOUND")));

            if (!post.getCreatedById().equals(userId)) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED_POST_ACCESS"));
            }

            if (post.getStatus() == PostStatus.CLOSED) {
                throw new AbstractException(messagesUtil.getMessage("POST_ALREADY_CLOSED"));
            }

            post.setStatus(PostStatus.CLOSED);
            post.setUpdatedAt(LocalDateTime.now());
            post = postRepository.save(post);
            return toPrivateOrderResponseDto(post);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error closing private order: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("POST_CLOSE_FAIL"));
        }
    }

    @Override
    public PrivateOrderResponseDto completePrivateOrder(String privateOrderId) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getOwnerIdFromToken(token);

            Post post = postRepository.findById(privateOrderId)
                    .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("POST_NOT_FOUND")));

            if (!post.getCreatedById().equals(userId)) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED_POST_ACCESS"));
            }

            if (post.getStatus() == PostStatus.COMPLETED) {
                throw new AbstractException(messagesUtil.getMessage("POST_ALREADY_COMPLETED"));
            }

            post.setStatus(PostStatus.COMPLETED);
            post.setUpdatedAt(LocalDateTime.now());
            post = postRepository.save(post);
            return toPrivateOrderResponseDto(post);
        } catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error completing private order: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("POST_COMPLETE_FAIL"));
        }
    }

    @Override
    public Page<PrivateOrderResponseDto> getMyPrivateOrders(Pageable pageable) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getOwnerIdFromToken(token);

            Page<Post> posts = postRepository.findByCreatedByIdOrderByCreatedAtDesc(userId, pageable);
            return posts.map(this::toPrivateOrderResponseDto);
        } catch (Exception e) {
            log.error("Error fetching user's private orders: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("POST_FETCH_MY_POSTS_FAIL"));
        }
    }

    @Override
    public Page<OfferResponseDto> getMyOffers(Pageable pageable) {
        try {
            String token = messagesUtil.getAuthToken();
            String userId = tokenProvider.getOwnerIdFromToken(token);
            Page<Offer> offers = offerRepository.findByOfferedByIdOrderByCreatedAtDesc(pageable, userId);
            return offers.map(this::toOfferResponseDto);
        } catch (Exception e) {
            log.error("Error fetching user's offers: {}", e.getMessage(), e);
            throw new AbstractException(messagesUtil.getMessage("OFFER_FETCH_MY_OFFERS_FAIL"));
        }
    }

    private PostType mapTargetType(PrivateOrderTargetType targetType) {
        if (targetType == null) return PostType.BOTH;
        return switch (targetType) {
            case SUPPLIERS -> PostType.SUPPLIERS;
            case CUSTOMERS -> PostType.CUSTOMERS;
            case BOTH -> PostType.BOTH;
        };
    }

    private PrivateOrderTargetType mapTargetType(PostType postType) {
        if (postType == null) return PrivateOrderTargetType.BOTH;
        return switch (postType) {
            case SUPPLIERS -> PrivateOrderTargetType.SUPPLIERS;
            case CUSTOMERS -> PrivateOrderTargetType.CUSTOMERS;
            case BOTH -> PrivateOrderTargetType.BOTH;
        };
    }

    private PrivateOrderResponseDto toPrivateOrderResponseDto(Post post) {
        List<Offer> offers = offerRepository.findByPostIdOrderByCreatedAtDesc(post.getId());
        List<OfferResponseDto> offerDtos = offers.stream()
                .map(this::toOfferResponseDto)
                .collect(Collectors.toList());

        return PrivateOrderResponseDto.builder()
                .id(post.getId())
                .materialName(post.getMaterialName())
                .image(post.getImage())
                .quantity(post.getQuantity())
                .unit(post.getUnit())
                .targetType(mapTargetType(post.getPostType()))
                .createdById(post.getCreatedById())
                .createdByName(post.getCreatedByName())
                .createdByOrganizationName(post.getCreatedByOrganizationName())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .active(post.isActive())
                .status(post.getStatus())
                .offers(offerDtos)
                .build();
    }

    private OfferResponseDto toOfferResponseDto(Offer offer) {
        return OfferResponseDto.builder()
                .id(offer.getId())
                .postId(offer.getPostId())
                .offeredById(offer.getOfferedById())
                .offeredByName(offer.getOfferedByName())
                .offeredByOrganizationName(offer.getOfferedByOrganizationName())
                .price(offer.getPrice())
                .availableQuantity(offer.getAvailableQuantity())
                .shippingInfo(offer.getShippingInfo())
                .estimatedDelivery(offer.getEstimatedDelivery())
                .notes(offer.getNotes())
                .accepted(offer.getAccepted())
                .responseMessage(offer.getResponseMessage())
                .respondedAt(offer.getRespondedAt())
                .createdAt(offer.getCreatedAt())
                .build();
    }
}
