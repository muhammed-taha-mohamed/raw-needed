package com.rawneeded.repository;

import com.rawneeded.model.Advertisement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdvertisementRepository extends MongoRepository<Advertisement, String> {

    /** For client display: active and not expired or no endDate (legacy); order via Pageable */
    @Query("{ 'active': true, $or: [ { 'endDate': { $gte: ?0 } }, { 'endDate': null } ] }")
    Page<Advertisement> findActiveAndNotExpired(LocalDateTime now, Pageable pageable);

    /** For client display: active, not expired, and not hidden */
    @Query("{ 'active': true, 'hidden': false, $or: [ { 'endDate': { $gt: ?0 } }, { 'endDate': null } ] }")
    Page<Advertisement> findActiveAndNotExpiredAndNotHidden(LocalDateTime now, Pageable pageable);

    Page<Advertisement> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    List<Advertisement> findByUserIdOrderByCreatedAtDesc(String userId);
    Page<Advertisement> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Optional<Advertisement> findByIdAndUserId(String id, String userId);
}
