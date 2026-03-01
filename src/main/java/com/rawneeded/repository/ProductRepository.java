package com.rawneeded.repository;

import com.rawneeded.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    long countBySupplier_Id(String supplierId);
    long countBySupplier_IdAndInStockTrue(String supplierId);
}
