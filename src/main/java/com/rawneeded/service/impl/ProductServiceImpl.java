package com.rawneeded.service.impl;

import com.rawneeded.dto.product.BulkUploadResultDto;
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
import com.rawneeded.repository.SubCategoryRepository;
import com.rawneeded.service.IProductService;
import com.rawneeded.service.IUserSubscriptionService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final SubCategoryRepository subCategoryRepository;
    private final JwtTokenProvider tokenProvider;
    private final IUserSubscriptionService userSubscriptionService;


    @Override
    public ProductResponseDTO create(ProductRequestDTO dto) {
        try {
            String token = messagesUtil.getAuthToken();
            String supplierId = tokenProvider.getOwnerIdFromToken(token);
            dto.setSupplierId(supplierId);

            log.info("Start creating a product: {}", dto);
            
            // Check if product with same name exists for this supplier
            checkProductNameUnique(supplierId, dto.getName(), null);
            
            Product product = productMapper.toEntity(dto);

            // set category
            setCategory(product, dto);

            return productMapper.toResponseDto(productRepository.save(product));
        } catch (AbstractException e) {
            throw e;
        } catch (DuplicateKeyException e) {
            log.info("Duplicate product name for supplier: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PRODUCT_NAME_EXISTS_FOR_SUPPLIER"));
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
                Product existingProduct = product.get();
                String supplierId = existingProduct.getSupplier() != null ? existingProduct.getSupplier().getId() : null;
                
                // Check if product name is being changed and if it conflicts with existing product
                if (dto.getName() != null && !dto.getName().equals(existingProduct.getName())) {
                    checkProductNameUnique(supplierId, dto.getName(), id);
                }
                
                Product newProduct = product.get();
                productMapper.update(newProduct, dto);

                // set category
                setCategory(newProduct, dto);

                return productMapper.toResponseDto(productRepository.save(newProduct));
            } else {
                throw new AbstractException(messagesUtil.getMessage("PRODUCT_NOT_FOUND"));
            }
        } catch (AbstractException e) {
            throw e;
        } catch (DuplicateKeyException e) {
            log.info("Duplicate product name for supplier: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("PRODUCT_NAME_EXISTS_FOR_SUPPLIER"));
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
            String userId = tokenProvider.getOwnerIdFromToken(token);

            // Check if user is customer or customer staff - need to deduct search
            if (role == Role.CUSTOMER_OWNER || role == Role.CUSTOMER_STAFF) {
                // Deduct search and add points
                boolean canSearch = userSubscriptionService.deductSearchAndAddPoints(userId);
                if (!canSearch) {
                    throw new AbstractException(messagesUtil.getMessage("NO_SEARCHES_OR_POINTS_AVAILABLE"));
                }
            }

            // if user is supplier then set supplierId from token
            if(role == Role.SUPPLIER_OWNER || role == Role.SUPPLIER_STAFF) {
                filterDTO.setSupplierId(userId);
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

    private void checkProductNameUnique(String supplierId, String productName, String excludeProductId) {
        if (supplierId == null || productName == null) {
            return;
        }
        
        Query query = new Query();
        query.addCriteria(Criteria.where("supplier.$id").is(new ObjectId(supplierId)));
        query.addCriteria(Criteria.where("name").is(productName));
        
        if (excludeProductId != null) {
            query.addCriteria(Criteria.where("id").ne(excludeProductId));
        }
        
        boolean exists = mongoTemplate.exists(query, Product.class);
        if (exists) {
            throw new AbstractException(messagesUtil.getMessage("PRODUCT_NAME_EXISTS_FOR_SUPPLIER"));
        }
    }

    @Override
    public Resource exportStock() {
        try {
            String token = messagesUtil.getAuthToken();
            Role role = tokenProvider.getRoleFromToken(token);
            String supplierId = tokenProvider.getOwnerIdFromToken(token);

            // Check if user is supplier
            if (role != Role.SUPPLIER_OWNER && role != Role.SUPPLIER_STAFF) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED_ACCESS"));
            }

            // Get all products for this supplier
            Query query = new Query();
            query.addCriteria(Criteria.where("supplier.$id").is(new ObjectId(supplierId)));
            List<Product> products = mongoTemplate.find(query, Product.class);

            // Create Excel workbook
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Stock Report");

            // Define colors (#009aa7 and #003259)
            XSSFColor primaryColor = new XSSFColor(new java.awt.Color(0, 154, 167), null);
            XSSFColor secondaryColor = new XSSFColor(new java.awt.Color(0, 50, 89), null);

            // Create styles
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(primaryColor);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFillForegroundColor(secondaryColor);
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setWrapText(true);

            CellStyle headerDataStyle = workbook.createCellStyle();
            headerDataStyle.cloneStyleFrom(dataStyle);
            headerDataStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(240, 240, 240), null));
            headerDataStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerDataStyle.setFont(headerFont);

            // Create title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Stock Report - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
            titleRow.setHeightInPoints(30);

            // Create header row
            Row headerRow = sheet.createRow(1);
            String[] headers = {"#", "Product Name", "Origin", "Category", "Sub Category", 
                               "Unit", "In Stock", "Stock Quantity", "Production Date", "Expiration Date"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            headerRow.setHeightInPoints(25);

            // Create data rows
            int rowNum = 2;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);
                Row row = sheet.createRow(rowNum++);
                
                // Row number
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(i + 1);
                cell0.setCellStyle(dataStyle);
                
                // Product Name
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(product.getName() != null ? product.getName() : "");
                cell1.setCellStyle(dataStyle);
                
                // Origin
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(product.getOrigin() != null ? product.getOrigin() : "");
                cell2.setCellStyle(dataStyle);
                
                // Category
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(product.getCategory() != null && product.getCategory().getName() != null 
                    ? product.getCategory().getName() : "");
                cell3.setCellStyle(dataStyle);
                
                // Sub Category
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(product.getSubCategory() != null && product.getSubCategory().getName() != null 
                    ? product.getSubCategory().getName() : "");
                cell4.setCellStyle(dataStyle);
                
                // Unit
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(product.getUnit() != null ? product.getUnit() : "");
                cell5.setCellStyle(dataStyle);
                
                // In Stock
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(product.isInStock() ? "Yes" : "No");
                cell6.setCellStyle(dataStyle);
                
                // Stock Quantity
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(product.getStockQuantity() != null ? product.getStockQuantity() : 0);
                cell7.setCellStyle(dataStyle);
                
                // Production Date
                Cell cell8 = row.createCell(8);
                cell8.setCellValue(product.getProductionDate() != null 
                    ? product.getProductionDate().format(dateFormatter) : "");
                cell8.setCellStyle(dataStyle);
                
                // Expiration Date
                Cell cell9 = row.createCell(9);
                cell9.setCellValue(product.getExpirationDate() != null 
                    ? product.getExpirationDate().format(dateFormatter) : "");
                cell9.setCellStyle(dataStyle);
                
                row.setHeightInPoints(20);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Add some padding
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            // Write workbook to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            return new ByteArrayResource(outputStream.toByteArray());

        } catch (AbstractException e) {
            throw e;
        } catch (IOException e) {
            log.error("Error creating Excel file: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("EXPORT_STOCK_FAIL"));
        } catch (Exception e) {
            log.error("An error occurred while exporting stock: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("EXPORT_STOCK_FAIL"));
        }
    }

    @Override
    public Resource downloadTemplate() {
        try {
            String token = messagesUtil.getAuthToken();
            Role role = tokenProvider.getRoleFromToken(token);
            if (role != Role.SUPPLIER_OWNER && role != Role.SUPPLIER_STAFF) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED_ACCESS"));
            }

            List<Category> categories = categoryRepository.findAll();
            XSSFWorkbook workbook = new XSSFWorkbook();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Sheet 0: Categories List (hidden - for dropdown) — A=Name, B=ID
            Sheet categoriesSheet = workbook.createSheet("_Categories");
            workbook.setSheetHidden(workbook.getSheetIndex(categoriesSheet), true);
            Row catHeaderRow = categoriesSheet.createRow(0);
            catHeaderRow.createCell(0).setCellValue("Category Name");
            catHeaderRow.createCell(1).setCellValue("Category ID");
            int catRowNum = 1;
            for (Category cat : categories) {
                Row row = categoriesSheet.createRow(catRowNum++);
                row.createCell(0).setCellValue(cat.getName() != null ? cat.getName() : "");
                row.createCell(1).setCellValue(cat.getId() != null ? cat.getId() : "");
            }
            String categoriesRange = "_Categories!$A$2:$A$" + catRowNum;

            // Sheet 1: SubCategories by Category (hidden) — A=Category Name, B=SubCategory Name, C=Key (A|B), D=SubCategory ID
            Sheet subCategoriesSheet = workbook.createSheet("_SubCategories");
            workbook.setSheetHidden(workbook.getSheetIndex(subCategoriesSheet), true);
            Row subCatHeaderRow = subCategoriesSheet.createRow(0);
            subCatHeaderRow.createCell(0).setCellValue("Category");
            subCatHeaderRow.createCell(1).setCellValue("SubCategory");
            subCatHeaderRow.createCell(2).setCellValue("Key");
            subCatHeaderRow.createCell(3).setCellValue("SubCategory ID");
            
            int subCatRowNum = 1;
            
            for (Category cat : categories) {
                List<SubCategory> subCategories = subCategoryRepository.findByCategoryId(cat.getId());
                if (!subCategories.isEmpty()) {
                    String categoryName = cat.getName() != null ? cat.getName() : "";
                    String cleanCategoryName = categoryName.replaceAll("[^A-Za-z0-9]", "_");
                    String rangeName = "SubCat_" + cleanCategoryName;
                    
                    int startRow = subCatRowNum + 1;
                    for (SubCategory sub : subCategories) {
                        Row row = subCategoriesSheet.createRow(subCatRowNum++);
                        String subName = sub.getName() != null ? sub.getName() : "";
                        row.createCell(0).setCellValue(categoryName);
                        row.createCell(1).setCellValue(subName);
                        row.createCell(2).setCellValue(categoryName + "|" + subName);
                        row.createCell(3).setCellValue(sub.getId() != null ? sub.getId() : "");
                    }
                    int endRow = subCatRowNum;
                    
                    Name namedRange = workbook.createName();
                    namedRange.setNameName(rangeName);
                    String formula = "_SubCategories!$B$" + startRow + ":$B$" + endRow;
                    namedRange.setRefersToFormula(formula);
                }
            }

            // Sheet 2: Products template
            XSSFSheet productsSheet = workbook.createSheet("Products");
            XSSFColor primaryColor = new XSSFColor(new java.awt.Color(0, 154, 167), null);
            XSSFColor secondaryColor = new XSSFColor(new java.awt.Color(0, 50, 89), null);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(primaryColor);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFillForegroundColor(secondaryColor);
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            Row titleRow = productsSheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Products Template - Select Category and SubCategory from dropdown lists. IDs are filled automatically and sent to backend.");
            titleCell.setCellStyle(titleStyle);
            productsSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));

            // Columns: 0-8 visible, 9-10 Category ID & SubCategory ID (hidden - sent to backend)
            String[] headers = {"Product Name*", "Origin", "Category Name*", "SubCategory Name*", "Unit", "In Stock (Yes/No)", "Stock Quantity", "Production Date (dd/MM/yyyy)", "Expiration Date (dd/MM/yyyy)", "Category ID", "SubCategory ID"};
            Row headerRow = productsSheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Hide columns 9 and 10 (Category ID, SubCategory ID)
            productsSheet.setColumnHidden(9, true);
            productsSheet.setColumnHidden(10, true);

            // First data row (row index 2 = Excel row 3) with example + formulas
            Row exampleRow = productsSheet.createRow(2);
            exampleRow.createCell(0).setCellValue("Example Product");
            exampleRow.createCell(1).setCellValue("Egypt");
            exampleRow.createCell(2).setCellValue("");
            exampleRow.createCell(3).setCellValue("");
            exampleRow.createCell(4).setCellValue("kg");
            exampleRow.createCell(5).setCellValue("Yes");
            exampleRow.createCell(6).setCellValue("100");
            exampleRow.createCell(7).setCellValue("");
            exampleRow.createCell(8).setCellValue("");
            exampleRow.createCell(9).setCellFormula("IF(C3=\"\",\"\",VLOOKUP(C3,_Categories!$A$2:$B$" + catRowNum + ",2,FALSE))");
            exampleRow.createCell(10).setCellFormula("IF(OR(C3=\"\",D3=\"\"),\"\",VLOOKUP(C3&\"|\"&D3,_SubCategories!$C$2:$D$" + subCatRowNum + ",2,FALSE))");

            // Rows 3-500: empty with formulas so user can fill; formulas reference current row
            for (int r = 3; r <= 500; r++) {
                Row row = productsSheet.createRow(r);
                row.createCell(9).setCellFormula("IF(C" + (r + 1) + "=\"\",\"\",VLOOKUP(C" + (r + 1) + ",_Categories!$A$2:$B$" + catRowNum + ",2,FALSE))");
                row.createCell(10).setCellFormula("IF(OR(C" + (r + 1) + "=\"\",D" + (r + 1) + "=\"\"),\"\",VLOOKUP(C" + (r + 1) + "&\"|\"&D" + (r + 1) + ",_SubCategories!$C$2:$D$" + subCatRowNum + ",2,FALSE))");
            }

            // Create Data Validation Helper
            XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(productsSheet);
            
            // Dropdown for Category Name (Column C, starting from row 2)
            DataValidationConstraint categoryConstraint = dvHelper.createFormulaListConstraint(categoriesRange);
            CellRangeAddressList categoryAddressList = new CellRangeAddressList(2, 10000, 2, 2); // Column C (index 2)
            XSSFDataValidation categoryValidation = (XSSFDataValidation) dvHelper.createValidation(categoryConstraint, categoryAddressList);
            categoryValidation.setShowErrorBox(true);
            categoryValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            categoryValidation.createErrorBox("Invalid Category", "Please select a category from the dropdown list.");
            categoryValidation.setSuppressDropDownArrow(true);
            productsSheet.addValidationData(categoryValidation);

            // Dropdown for SubCategory Name (Column D, starting from row 2) - Dependent on Category
            // Using INDIRECT with SUBSTITUTE to clean category name and reference named range
            // Formula: INDIRECT("SubCat_"&SUBSTITUTE(SUBSTITUTE(SUBSTITUTE(C3," ","_"),"-","_"),"/","_"))
            // This handles spaces, hyphens, and slashes in category names
            String subCategoryFormula = "INDIRECT(\"SubCat_\"&SUBSTITUTE(SUBSTITUTE(SUBSTITUTE(C3,\" \",\"_\"),\"-\",\"_\"),\"/\",\"_\"))";
            DataValidationConstraint subCategoryConstraint = dvHelper.createFormulaListConstraint(subCategoryFormula);
            CellRangeAddressList subCategoryAddressList = new CellRangeAddressList(2, 10000, 3, 3); // Column D (index 3), rows 3-10001
            XSSFDataValidation subCategoryValidation = (XSSFDataValidation) dvHelper.createValidation(subCategoryConstraint, subCategoryAddressList);
            subCategoryValidation.setShowErrorBox(true);
            subCategoryValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            subCategoryValidation.createErrorBox("Invalid SubCategory", "Please select a subcategory from the dropdown list. Make sure you selected a Category first.");
            subCategoryValidation.setSuppressDropDownArrow(true);
            productsSheet.addValidationData(subCategoryValidation);

            // Dropdown for In Stock (Column F, starting from row 2) - Yes/No
            String[] yesNoOptions = {"Yes", "No"};
            DataValidationConstraint yesNoConstraint = dvHelper.createExplicitListConstraint(yesNoOptions);
            CellRangeAddressList yesNoAddressList = new CellRangeAddressList(2, 10000, 5, 5); // Column F (index 5)
            XSSFDataValidation yesNoValidation = (XSSFDataValidation) dvHelper.createValidation(yesNoConstraint, yesNoAddressList);
            yesNoValidation.setShowErrorBox(true);
            yesNoValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            yesNoValidation.createErrorBox("Invalid Value", "Please select 'Yes' or 'No'.");
            yesNoValidation.setSuppressDropDownArrow(true);
            productsSheet.addValidationData(yesNoValidation);

            for (int i = 0; i < headers.length; i++) {
                productsSheet.autoSizeColumn(i);
                productsSheet.setColumnWidth(i, Math.min(productsSheet.getColumnWidth(i) + 500, 15000));
            }

            // Sheet 3: Categories Reference (visible - for reference only)
            Sheet refSheet = workbook.createSheet("Categories Reference");
            Row refTitleRow = refSheet.createRow(0);
            refTitleRow.createCell(0).setCellValue("Category Name");
            refTitleRow.createCell(1).setCellValue("SubCategory Name");
            refTitleRow.getCell(0).setCellStyle(headerStyle);
            refTitleRow.getCell(1).setCellStyle(headerStyle);

            int refRowNum = 1;
            for (Category cat : categories) {
                List<SubCategory> subCategories = subCategoryRepository.findByCategoryId(cat.getId());
                if (subCategories.isEmpty()) {
                    Row r = refSheet.createRow(refRowNum++);
                    r.createCell(0).setCellValue(cat.getName() != null ? cat.getName() : "");
                    r.createCell(1).setCellValue("");
                } else {
                    for (SubCategory sub : subCategories) {
                        Row r = refSheet.createRow(refRowNum++);
                        r.createCell(0).setCellValue(cat.getName() != null ? cat.getName() : "");
                        r.createCell(1).setCellValue(sub.getName() != null ? sub.getName() : "");
                    }
                }
            }
            refSheet.autoSizeColumn(0);
            refSheet.autoSizeColumn(1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();
            return new ByteArrayResource(out.toByteArray());
        } catch (AbstractException e) {
            throw e;
        } catch (IOException e) {
            log.error("Error creating template: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("DOWNLOAD_TEMPLATE_FAIL"));
        } catch (Exception e) {
            log.error("Error creating template: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("DOWNLOAD_TEMPLATE_FAIL"));
        }
    }

    @Override
    public BulkUploadResultDto uploadProducts(MultipartFile file) {
        try {
            String token = messagesUtil.getAuthToken();
            Role role = tokenProvider.getRoleFromToken(token);
            String supplierId = tokenProvider.getOwnerIdFromToken(token);
            if (role != Role.SUPPLIER_OWNER && role != Role.SUPPLIER_STAFF) {
                throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED_ACCESS"));
            }

            if (file.isEmpty() || !isExcelFile(file.getOriginalFilename())) {
                throw new AbstractException(messagesUtil.getMessage("INVALID_EXCEL_FILE"));
            }

            List<BulkUploadResultDto.RowErrorDto> errors = new ArrayList<>();
            int successCount = 0;
            int rowsProcessed = 0;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            try (InputStream is = file.getInputStream();
                 Workbook workbook = new XSSFWorkbook(is)) {
                Sheet sheet = workbook.getSheet("Products");
                if (sheet == null) {
                    sheet = workbook.getSheetAt(0);
                }
                int lastRowNum = sheet.getLastRowNum();
                if (lastRowNum < 2) {
                    return BulkUploadResultDto.builder()
                            .totalRows(0)
                            .successCount(0)
                            .failedCount(0)
                            .errors(List.of())
                            .build();
                }

                for (int rowIndex = 2; rowIndex <= lastRowNum; rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;

                    String productName = getCellString(row.getCell(0));
                    if (productName == null || productName.isBlank()) continue;
                    rowsProcessed++;

                    String origin = getCellString(row.getCell(1));
                    String categoryName = getCellString(row.getCell(2));
                    String subCategoryName = getCellString(row.getCell(3));
                    String unit = getCellString(row.getCell(4));
                    String inStockStr = getCellString(row.getCell(5));
                    String stockQtyStr = getCellString(row.getCell(6));
                    String productionDateStr = getCellString(row.getCell(7));
                    String expirationDateStr = getCellString(row.getCell(8));
                    // Columns 9 & 10: Category ID and SubCategory ID (filled by formula when user selects from dropdown)
                    String categoryIdFromSheet = getCellString(row.getCell(9));
                    String subCategoryIdFromSheet = getCellString(row.getCell(10));


                    ProductRequestDTO dto = new ProductRequestDTO();
                    dto.setSupplierId(supplierId);
                    dto.setName(productName.trim());
                    dto.setOrigin(origin != null ? origin.trim() : null);
                    dto.setCategoryId(categoryIdFromSheet);
                    dto.setSubCategoryId(subCategoryIdFromSheet);
                    dto.setUnit(unit != null && !unit.isBlank() ? unit.trim() : null);
                    dto.setInStock(parseYesNo(inStockStr));
                    dto.setStockQuantity(parseIntSafe(stockQtyStr));
                    dto.setProductionDate(parseDateSafe(productionDateStr, dateFormatter));
                    dto.setExpirationDate(parseDateSafe(expirationDateStr, dateFormatter));

                    try {
                        checkProductNameUnique(supplierId, dto.getName(), null);
                        Product product = productMapper.toEntity(dto);
                        setCategory(product, dto);
                        if (product.getSupplier() == null) {
                            product.setSupplier(User.builder().id(supplierId).build());
                        }
                        productRepository.save(product);
                        successCount++;
                    } catch (AbstractException e) {
                        errors.add(BulkUploadResultDto.RowErrorDto.builder()
                                .rowNumber(rowIndex + 1)
                                .productName(productName)
                                .errorMessage(e.getMessage())
                                .build());
                    } catch (Exception e) {
                        errors.add(BulkUploadResultDto.RowErrorDto.builder()
                                .rowNumber(rowIndex + 1)
                                .productName(productName)
                                .errorMessage(e.getMessage() != null ? e.getMessage() : "Unknown error")
                                .build());
                    }
                }
            }

            return BulkUploadResultDto.builder()
                    .totalRows(rowsProcessed)
                    .successCount(successCount)
                    .failedCount(errors.size())
                    .errors(errors)
                    .build();
        } catch (AbstractException e) {
            throw e;
        } catch (IOException e) {
            log.error("Error reading upload file: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("UPLOAD_PRODUCTS_FAIL"));
        } catch (Exception e) {
            log.error("Error uploading products: {}", e.getMessage());
            throw new AbstractException(messagesUtil.getMessage("UPLOAD_PRODUCTS_FAIL"));
        }
    }

    private boolean isExcelFile(String filename) {
        if (filename == null) return false;
        return filename.endsWith(".xlsx") || filename.endsWith(".xls");
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                double num = cell.getNumericCellValue();
                yield num == (long) num ? String.valueOf((long) num) : String.valueOf(num);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private boolean parseYesNo(String value) {
        if (value == null || value.isBlank()) return true;
        return value.trim().equalsIgnoreCase("yes") || value.trim().equalsIgnoreCase("y") || value.trim().equals("1");
    }

    private Integer parseIntSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.trim().replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseDateSafe(String value, DateTimeFormatter formatter) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value.trim(), formatter);
        } catch (Exception e) {
            try {
                return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e2) {
                return null;
            }
        }
    }
}



