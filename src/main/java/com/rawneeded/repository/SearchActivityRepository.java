package com.rawneeded.repository;

import com.rawneeded.model.SearchActivity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SearchActivityRepository extends MongoRepository<SearchActivity, String> {
}
