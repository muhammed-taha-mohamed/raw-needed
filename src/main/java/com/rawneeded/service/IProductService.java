package com.rawneeded.service;

import com.rawneeded.dto.product.BulkUploadResultDto;
import com.rawneeded.dto.product.ProductFilterDTO;
import com.rawneeded.dto.product.ProductRequestDTO;
import com.rawneeded.dto.product.ProductResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;


public interface IProductService {


    ProductResponseDTO create (ProductRequestDTO dto);


    ProductResponseDTO update(String id, ProductRequestDTO dto);

    void delete(String id);

    Page<ProductResponseDTO> filter (Pageable pageable, ProductFilterDTO filterDTO);

    Resource exportStock();

    Resource downloadTemplate();

    BulkUploadResultDto uploadProducts(MultipartFile file);
}
