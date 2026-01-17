package com.rawneeded.repository;

import com.rawneeded.model.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends MongoRepository<Offer, String> {
    List<Offer> findByPostId(String postId);
    List<Offer> findByPostIdOrderByCreatedAtDesc(String postId);
    Page<Offer> findByOfferedByIdOrderByCreatedAtDesc(Pageable pageable, String offeredById);

}
