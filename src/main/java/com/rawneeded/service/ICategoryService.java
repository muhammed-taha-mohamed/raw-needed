package com.rawneeded.service;

import com.rawneeded.dto.category.CategoryRequestDto;
import com.rawneeded.dto.category.CategoryResponseDto;
import com.rawneeded.dto.category.SubCategoryRequestDto;
import com.rawneeded.dto.category.SubCategoryResponseDto;

import java.util.List;

public interface ICategoryService {

    // ===== Category =====
    CategoryResponseDto createCategory(CategoryRequestDto dto);

    CategoryResponseDto updateCategory(String id, CategoryRequestDto dto);

    void deleteCategory(String id);

    CategoryResponseDto getCategoryById(String id);

    List<CategoryResponseDto> getAllCategories();


    // ===== SubCategory =====
    SubCategoryResponseDto createSubCategory(SubCategoryRequestDto dto);

    SubCategoryResponseDto updateSubCategory(String id, SubCategoryRequestDto dto);

    void deleteSubCategory(String id);

    SubCategoryResponseDto getSubCategoryById(String id);

    List<SubCategoryResponseDto> getSubCategoriesByCategoryId(String categoryId);
}
