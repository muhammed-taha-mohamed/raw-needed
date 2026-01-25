package com.rawneeded.repository;

import com.rawneeded.model.ComplaintMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintMessageRepository extends MongoRepository<ComplaintMessage, String> {
    List<ComplaintMessage> findByComplaintIdOrderByCreatedAtAsc(String complaintId);
}
