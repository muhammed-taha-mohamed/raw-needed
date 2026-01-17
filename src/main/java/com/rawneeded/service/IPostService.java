package com.rawneeded.service;

import com.rawneeded.dto.post.CreateOfferRequest;
import com.rawneeded.dto.post.CreatePostRequest;
import com.rawneeded.dto.post.OfferResponseDto;
import com.rawneeded.dto.post.PostResponseDto;
import com.rawneeded.dto.post.RespondToOfferRequest;
import com.rawneeded.enumeration.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPostService {
    PostResponseDto createPost(CreatePostRequest request);
    Page<PostResponseDto> getAllPosts(Pageable pageable);

    List<PostResponseDto> myPosts();

    PostResponseDto getPostById(String postId);
    OfferResponseDto createOffer(String postId, CreateOfferRequest request);
    OfferResponseDto respondToOffer(String postId, String offerId, RespondToOfferRequest request);
    PostResponseDto closePost(String postId);
    PostResponseDto completePost(String postId);
    Page<PostResponseDto> getMyPosts(Pageable pageable);

    Page<OfferResponseDto> getMyOffers(Pageable pageable);
}
