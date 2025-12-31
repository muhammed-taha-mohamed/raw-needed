package com.rawneeded.repository;

import com.rawneeded.model.PaymentQuotation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentQuotationRepository extends MongoRepository<PaymentQuotation, String> {
    List<PaymentQuotation> findByOwnerId(String ownerId);
    Optional<PaymentQuotation> findByOwnerIdAndStatus(String ownerId, com.rawneeded.enumeration.QuotationStatus status);
}
