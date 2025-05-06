package com.rawneeded.service.impl;

import com.rawneeded.dto.product.ProductFilterDTO;
import com.rawneeded.dto.product.ProductRequestDTO;
import com.rawneeded.dto.product.ProductResponseDTO;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.mapper.ProductMapper;
import com.rawneeded.model.Product;
import com.rawneeded.model.User;
import com.rawneeded.repository.ProductRepository;
import com.rawneeded.service.IProductService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
@AllArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final MessagesUtil messagesUtil;
    private final NotificationService notificationService;
    private final ProductMapper productMapper;
    private final MongoTemplate mongoTemplate;


    @Override
    public ProductResponseDTO create(ProductRequestDTO dto) {
        try {
            log.info("Start creating a product: {}", dto);
            Product product = productMapper.toEntity(dto);
            return productMapper.toResponseDto(productRepository.save(product));
        } catch (Exception e) {
            log.info("An error occurred while creating a product: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public ProductResponseDTO update(String id, ProductRequestDTO dto) {
        try {
            log.info("Start updating a product: {}", dto);
            Optional<Product> product = productRepository.findById(id);
            if (product.isPresent()) {
                Product newProduct = product.get();
                productMapper.update(newProduct, dto);
                return productMapper.toResponseDto(productRepository.save(newProduct));
            } else {
                throw new AbstractException("Product not found");
            }
        } catch (Exception e) {
            log.info("An error occurred while updating a product: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public void delete(String id) {
        try {
            log.info("Start deleting a product: {}", id);
            productRepository.deleteById(id);
        } catch (Exception e) {
            log.info("An error occurred while deleting a product: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public Page<ProductResponseDTO> filter(Pageable pageable, ProductFilterDTO filterDTO) {
        try {
            List<Criteria> criteriaList = new ArrayList<>();

            if (filterDTO.getName() != null && !filterDTO.getName().isEmpty()) {
                criteriaList.add(Criteria.where("name").regex(filterDTO.getName(), "i"));
            }

            if (filterDTO.getOrigin() != null && !filterDTO.getOrigin().isEmpty()) {
                criteriaList.add(Criteria.where("origin").is(filterDTO.getOrigin()));
            }

            if (filterDTO.getSupplierId() != null && !filterDTO.getSupplierId().isEmpty()) {
                criteriaList.add(Criteria.where("supplier.$id").is(new ObjectId(filterDTO.getSupplierId())));
            }

            if (filterDTO.getCategory() != null) {
                criteriaList.add(Criteria.where("category").is(filterDTO.getCategory()));
            }

            if (filterDTO.getSubCategory() != null) {
                criteriaList.add(Criteria.where("subCategory").is(filterDTO.getSubCategory()));
            }

            Query query = new Query();
            if (!criteriaList.isEmpty()) {
                query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
            }

            long total = mongoTemplate.count(query, Product.class);
            query.with(pageable);

            List<Product> products = mongoTemplate.find(query, Product.class);

            List<ProductResponseDTO> content = productMapper.toResponseList(products);

            return new PageImpl<>(content, pageable, total);

        } catch (Exception e) {
            throw new AbstractException(e.getMessage());
        }
    }

}



