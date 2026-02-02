package com.rawneeded.repository;

import com.rawneeded.model.SubCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubCategoryRepository extends MongoRepository<SubCategory, String> {

    List<SubCategory> findByCategoryId(String categoryId);

    Optional<SubCategory> findFirstByNameIgnoreCaseAndCategoryId(String name, String categoryId);

    boolean existsByNameIgnoreCaseAndCategoryId(String name, String categoryId);

    boolean existsByNameIgnoreCaseAndCategoryIdAndIdNot(
            String name,
            String categoryId,
            String id
    );

    List<SubCategory> findByIdIn(List<String> ids);
}
