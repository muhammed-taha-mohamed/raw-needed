package com.rawneeded.service.impl;

import com.rawneeded.dto.product.ProductFilterDTO;
import com.rawneeded.dto.product.ProductRequestDTO;
import com.rawneeded.dto.product.ProductResponseDTO;
import com.rawneeded.enumeration.Role;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.mapper.ProductMapper;
import com.rawneeded.model.Category;
import com.rawneeded.model.Product;
import com.rawneeded.model.SubCategory;
import com.rawneeded.model.User;
import com.rawneeded.repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;
    private final JwtTokenProvider tokenProvider;


    @Override
    public ProductResponseDTO create(ProductRequestDTO dto) {
        try {
            String token = messagesUtil.getAuthToken();
            String supplierId = tokenProvider.getOwnerIdFromToken(token);
            dto.setSupplierId(supplierId);

            log.info("Start creating a product: {}", dto);
            Product product = productMapper.toEntity(dto);

            // set category
            setCategory(product, dto);

            return productMapper.toResponseDto(productRepository.save(product));
        }catch (AbstractException e) {
            throw e;
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

                // set category
                setCategory(newProduct, dto);

                return productMapper.toResponseDto(productRepository.save(newProduct));
            } else {
                throw new AbstractException(messagesUtil.getMessage("PRODUCT_NOT_FOUND"));
            }
        }catch (AbstractException e) {
            throw e;
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
        }catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.info("An error occurred while deleting a product: {}", e.getMessage());
            throw new AbstractException(e.getMessage());
        }
    }

    @Override
    public Page<ProductResponseDTO> filter(Pageable pageable, ProductFilterDTO filterDTO) {
        try {

            String token = messagesUtil.getAuthToken();
            Role role = tokenProvider.getRoleFromToken(token);

            // if user is supplier then set supplierId from token
            if(role == Role.SUPPLIER_OWNER || role == Role.SUPPLIER_STAFF) {
                filterDTO.setSupplierId(tokenProvider.getOwnerIdFromToken(token));
            }

            List<Criteria> criteriaList = new ArrayList<>();

            if (filterDTO.getName() != null && !filterDTO.getName().isEmpty()) {
                criteriaList.add(Criteria.where("name").regex(filterDTO.getName(), "i"));
            }

            if (filterDTO.getOrigin() != null && !filterDTO.getOrigin().isEmpty()) {
                criteriaList.add(Criteria.where("origin").regex(filterDTO.getOrigin(), "i"));
            }

            if (filterDTO.getSupplierId() != null && !filterDTO.getSupplierId().isEmpty()) {
                criteriaList.add(Criteria.where("supplier.$id").is(new ObjectId(filterDTO.getSupplierId())));
            }

            if (filterDTO.getCategoryId() != null && !filterDTO.getCategoryId().isEmpty()) {
                criteriaList.add(Criteria.where("category.$id").is(new ObjectId(filterDTO.getCategoryId())));
            }

            if (filterDTO.getSubCategoryId() != null && !filterDTO.getSubCategoryId().isEmpty()) {
                criteriaList.add(Criteria.where("subCategory.$id").is(new ObjectId(filterDTO.getSubCategoryId())));
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

        }catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            throw new AbstractException(e.getMessage());
        }
    }


    private void setCategory(Product product , ProductRequestDTO dto) {
        // set category
        if(dto.getCategoryId() != null ) {
            product.setCategory(Category.builder()
                    .id(dto.getCategoryId())
                    .build());
        }

        // set sub category
        if(dto.getSubCategoryId() != null ) {
            product.setSubCategory(SubCategory.builder()
                    .id(dto.getSubCategoryId()).build());
        }
    }
}



