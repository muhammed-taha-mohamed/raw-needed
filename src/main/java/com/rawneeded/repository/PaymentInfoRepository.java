package com.rawneeded.repository;

import com.rawneeded.enumeration.PaymentType;
import com.rawneeded.model.PaymentInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentInfoRepository extends MongoRepository<PaymentInfo, String> {
    
    List<PaymentInfo> findByPaymentType(PaymentType paymentType);
    
    List<PaymentInfo> findByActiveTrue();
    
    List<PaymentInfo> findByPaymentTypeAndActiveTrue(PaymentType paymentType);
}
