package com.rawneeded.repository;

import com.rawneeded.model.SupplierSpecialOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SupplierSpecialOfferRepository extends MongoRepository<SupplierSpecialOffer, String> {
    Page<SupplierSpecialOffer> findBySupplierIdAndActiveTrueOrderByCreatedAtDesc(String supplierId, Pageable pageable);
    Page<SupplierSpecialOffer> findByActiveTrueAndStartDateBeforeAndEndDateAfterOrderByCreatedAtDesc(
            LocalDateTime now, LocalDateTime now2, Pageable pageable);
    List<SupplierSpecialOffer> findByProductIdAndActiveTrueAndStartDateBeforeAndEndDateAfter(
            String productId, LocalDateTime now, LocalDateTime now2);
    List<SupplierSpecialOffer> findBySupplierId(String supplierId);
}
