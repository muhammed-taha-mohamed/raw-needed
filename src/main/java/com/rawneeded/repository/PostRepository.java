package com.rawneeded.repository;

import com.rawneeded.enumeration.PostStatus;
import com.rawneeded.enumeration.PostType;
import com.rawneeded.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    Page<Post> findByPostTypeAndActiveTrueOrderByCreatedAtDesc(PostType postType, Pageable pageable);
    List<Post> findByCreatedById(String createdById);
    Page<Post> findByCreatedByIdOrderByCreatedAtDesc(String createdById, Pageable pageable);

}
