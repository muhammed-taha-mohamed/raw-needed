package com.rawneeded.repository;

import com.rawneeded.model.SubCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SubCategoryRepository extends MongoRepository<SubCategory, String> {

    List<SubCategory> findByCategoryId(String categoryId);

    boolean existsByNameIgnoreCaseAndCategoryId(String name, String categoryId);

    boolean existsByNameIgnoreCaseAndCategoryIdAndIdNot(
            String name,
            String categoryId,
            String id
    );

    List<SubCategory> findByIdIn(List<String> ids);
}
