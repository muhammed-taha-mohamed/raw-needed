package com.rawneeded.repository;

import com.rawneeded.enumeration.LineStatus;
import com.rawneeded.model.RFQOrderLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RFQOrderLineRepository extends MongoRepository<RFQOrderLine, String> {

    List<RFQOrderLine> findByOrderId(String orderId);

    Page<RFQOrderLine> findBySupplierIdAndStatus(Pageable pageable, String supplierId, LineStatus status);
    Page<RFQOrderLine> findBySupplierId(Pageable pageable,String supplierId);
}
