package com.rawneeded.repository;

import com.rawneeded.model.AdvertisementView;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertisementViewRepository extends MongoRepository<AdvertisementView, String> {
    
    long countByAdvertisementId(String advertisementId);
    
    List<AdvertisementView> findByAdvertisementIdOrderByViewedAtDesc(String advertisementId);
    
    @Query(value = "{ 'advertisementId': ?0, 'viewerId': ?1 }", exists = true)
    boolean existsByAdvertisementIdAndViewerId(String advertisementId, String viewerId);
    
    List<AdvertisementView> findByAdvertisementId(String advertisementId);
}
