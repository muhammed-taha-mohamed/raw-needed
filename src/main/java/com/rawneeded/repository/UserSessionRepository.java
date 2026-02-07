package com.rawneeded.repository;

import com.rawneeded.model.UserSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends MongoRepository<UserSession, String> {
    Optional<UserSession> findByUserId(String userId);
    void deleteByUserId(String userId);
}
