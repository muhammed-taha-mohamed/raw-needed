package com.rawneeded.repository;

import com.rawneeded.enumeration.ComplaintStatus;
import com.rawneeded.model.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends MongoRepository<Complaint, String> {
    Page<Complaint> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Page<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status, Pageable pageable);
    Page<Complaint> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Optional<Complaint> findByIdAndUserId(String id, String userId);
    List<Complaint> findByUserIdAndStatus(String userId, ComplaintStatus status);

    long countByStatus(ComplaintStatus status);

    List<Complaint> findFirst10ByOrderByCreatedAtDesc();
}
