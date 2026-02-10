package com.rawneeded.repository;

import com.rawneeded.enumeration.LineStatus;
import com.rawneeded.model.RFQOrderLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RFQOrderLineRepository extends MongoRepository<RFQOrderLine, String> {

    List<RFQOrderLine> findByOrderId(String orderId);

    Page<RFQOrderLine> findBySupplierIdAndStatusOrderByIdDesc(String supplierId, LineStatus status, Pageable pageable);
    Page<RFQOrderLine> findBySupplierIdOrderByIdDesc(String supplierId, Pageable pageable);
    
    // Statistics queries
    long countBySupplierId(String supplierId);
    long countBySupplierIdAndStatus(String supplierId, LineStatus status);
    long countByCustomerOwnerId(String customerOwnerId);
    
    List<RFQOrderLine> findBySupplierId(String supplierId);
    List<RFQOrderLine> findByCustomerOwnerId(String customerOwnerId);
}
