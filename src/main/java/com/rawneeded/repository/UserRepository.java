package com.rawneeded.repository;

import com.rawneeded.enumeration.Role;
import com.rawneeded.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmailIgnoreCase (String email);

    Optional<User> findByEmailAndForgetPasswordOTP(String email , String otp);

    boolean existsByRole(Role role);

    Page<User> findAllByOwnerId(String ownerId , Pageable pageable);
}
