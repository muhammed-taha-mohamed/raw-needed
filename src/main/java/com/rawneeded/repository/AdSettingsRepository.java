package com.rawneeded.repository;

import com.rawneeded.model.AdSettings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdSettingsRepository extends MongoRepository<AdSettings, String> {
}
