package com.rawneeded.service.impl;

import com.rawneeded.dto.category.CategoryRequestDto;
import com.rawneeded.dto.category.CategoryResponseDto;
import com.rawneeded.dto.category.SubCategoryRequestDto;
import com.rawneeded.dto.category.SubCategoryResponseDto;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.mapper.CategoryMapper;
import com.rawneeded.mapper.SubCategoryMapper;
import com.rawneeded.model.Category;
import com.rawneeded.model.SubCategory;
import com.rawneeded.repository.CategoryRepository;
import com.rawneeded.repository.SubCategoryRepository;
import com.rawneeded.service.ICategoryService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    private final CategoryMapper categoryMapper;
    private final SubCategoryMapper subCategoryMapper;
    private final MessagesUtil messagesUtil;

    // ================= Category =================

    @Override
    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new AbstractException(messagesUtil.getMessage("CATEGORY_EXISTS"));
        }

        Category category = categoryMapper.toEntity(dto);
        return categoryMapper.toResponseDto(categoryRepository.save(category));
    }

    @Override
    public CategoryResponseDto updateCategory(String id, CategoryRequestDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("CATEGORY_NOT_FOUND")));

        categoryMapper.update(category, dto);
        return categoryMapper.toResponseDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new AbstractException(messagesUtil.getMessage("CATEGORY_NOT_FOUND"));
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryResponseDto getCategoryById(String id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponseDto)
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("CATEGORY_NOT_FOUND")));
    }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponseDto)
                .toList();
    }

    // ================= SubCategory =================

    @Override
    public SubCategoryResponseDto createSubCategory(SubCategoryRequestDto dto) {

        if (!categoryRepository.existsById(dto.getCategoryId())) {
            throw new AbstractException(messagesUtil.getMessage("CATEGORY_NOT_FOUND"));
        }

        if (subCategoryRepository.existsByNameIgnoreCaseAndCategoryId(
                dto.getName(), dto.getCategoryId())) {
            throw new AbstractException(messagesUtil.getMessage("SUBCATEGORY_EXISTS"));
        }

        SubCategory subCategory = subCategoryMapper.toEntity(dto);
        return subCategoryMapper.toResponseDto(subCategoryRepository.save(subCategory));
    }

    @Override
    public SubCategoryResponseDto updateSubCategory(String id, SubCategoryRequestDto dto) {
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("SUBCATEGORY_NOT_FOUND")));

        subCategoryMapper.update(subCategory, dto);
        return subCategoryMapper.toResponseDto(subCategoryRepository.save(subCategory));
    }

    @Override
    public void deleteSubCategory(String id) {
        if (!subCategoryRepository.existsById(id)) {
            throw new AbstractException(messagesUtil.getMessage("SUBCATEGORY_NOT_FOUND"));
        }
        subCategoryRepository.deleteById(id);
    }

    @Override
    public SubCategoryResponseDto getSubCategoryById(String id) {
        return subCategoryRepository.findById(id)
                .map(subCategoryMapper::toResponseDto)
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("SUBCATEGORY_NOT_FOUND")));
    }

    @Override
    public List<SubCategoryResponseDto> getSubCategoriesByCategoryId(String categoryId) {
        return subCategoryRepository.findByCategoryId(categoryId)
                .stream()
                .map(subCategoryMapper::toResponseDto)
                .toList();
    }
}
