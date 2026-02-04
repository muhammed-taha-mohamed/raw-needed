package com.rawneeded.repository;

import com.rawneeded.model.AdPackage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdPackageRepository extends MongoRepository<AdPackage, String> {
    List<AdPackage> findByActiveTrueOrderBySortOrderAsc();
}
