package com.rawneeded.controller;

import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.dto.post.CreateOfferRequest;
import com.rawneeded.dto.post.CreatePostRequest;
import com.rawneeded.dto.post.OfferResponseDto;
import com.rawneeded.dto.post.PostResponseDto;
import com.rawneeded.dto.post.RespondToOfferRequest;
import com.rawneeded.enumeration.PostType;
import com.rawneeded.service.IPostService;
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
@RequestMapping("api/v1/posts")
public class PostController {

    private final IPostService postService;

    @PostMapping
    @Operation(
            summary = "Create a new post",
            description = "Customer creates a post requesting a specific material"
    )
    public ResponseEntity<ResponsePayload> createPost(
            @Valid @RequestBody CreatePostRequest request) {
        PostResponseDto post = postService.createPost(request);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", post,
                        "message", "Post created successfully"
                ))
                .build());
    }

    @GetMapping
    @Operation(
            summary = "Get posts by type",
            description = "Get posts filtered by type (SUPPLIERS, CUSTOMERS, or BOTH), ordered by creation date (newest first)"
    )
    public ResponseEntity<ResponsePayload> getPostsByType(
            @RequestParam(required = false) PostType postType,
            Pageable pageable) {
        Page<PostResponseDto> posts = postService.getAllPosts(pageable);

        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", posts
                ))
                .build());
    }

    @GetMapping("/{postId}")
    @Operation(
            summary = "Get post by ID",
            description = "Get a specific post with all its offers"
    )
    public ResponseEntity<ResponsePayload> getPostById(
            @PathVariable String postId) {
        PostResponseDto post = postService.getPostById(postId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", post
                ))
                .build());
    }

    @PostMapping("/{postId}/offers")
    @Operation(
            summary = "Create an offer for a post",
            description = "Supplier or customer creates an offer responding to a post"
    )
    public ResponseEntity<ResponsePayload> createOffer(
            @PathVariable String postId,
            @Valid @RequestBody CreateOfferRequest request) {
        OfferResponseDto offer = postService.createOffer(postId, request);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", offer,
                        "message", "Offer created successfully"
                ))
                .build());
    }

    @PostMapping("/offers/respond")
    @Operation(
            summary = "Respond to an offer",
            description = "Post creator responds to an offer (accept or reject)"
    )
    public ResponseEntity<ResponsePayload> respondToOffer(
            @RequestParam String postId,
            @RequestParam String offerId,
            @Valid @RequestBody RespondToOfferRequest request) {
        OfferResponseDto offer = postService.respondToOffer(postId, offerId, request);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", offer,
                        "message", "Offer response submitted successfully"
                ))
                .build());
    }

    @PutMapping("/{postId}/close")
    @Operation(
            summary = "Close a post",
            description = "Post creator closes the post (no longer accepting offers)"
    )
    public ResponseEntity<ResponsePayload> closePost(
            @PathVariable String postId) {
        PostResponseDto post = postService.closePost(postId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", post,
                        "message", "Post closed successfully"
                ))
                .build());
    }

    @PutMapping("/{postId}/complete")
    @Operation(
            summary = "Mark post as completed",
            description = "Post creator marks the post as completed (material received)"
    )
    public ResponseEntity<ResponsePayload> completePost(
            @PathVariable String postId) {
        PostResponseDto post = postService.completePost(postId);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", post,
                        "message", "Post marked as completed successfully"
                ))
                .build());
    }

    @GetMapping("/my-posts")
    @Operation(
            summary = "Get my posts : Customer",
            description = "Get all posts created by the current user, ordered by creation date (newest first)"
    )
    public ResponseEntity<ResponsePayload> getMyPosts(Pageable pageable) {
        Page<PostResponseDto> posts = postService.getMyPosts(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", posts
                ))
                .build());
    }

    @GetMapping("/my-offers")
    @Operation(
            summary = "Get my posts : Supplier",
            description = "Get all offers created by the current user, ordered by creation date (newest first)"
    )
    public ResponseEntity<ResponsePayload> getMyOffers(Pageable pageable) {
        Page<OfferResponseDto> posts = postService.getMyOffers(pageable);
        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "success", true,
                        "data", posts
                ))
                .build());
    }
}
