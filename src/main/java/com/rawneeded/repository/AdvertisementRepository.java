package com.rawneeded.repository;

import com.rawneeded.model.Advertisement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdvertisementRepository extends MongoRepository<Advertisement, String> {
    Page<Advertisement> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    List<Advertisement> findByUserIdOrderByCreatedAtDesc(String userId);
    Page<Advertisement> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Optional<Advertisement> findByIdAndUserId(String id, String userId);
}
