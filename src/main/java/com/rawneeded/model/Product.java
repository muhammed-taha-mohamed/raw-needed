package com.rawneeded.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document
@Builder
//@CompoundIndex(name = "supplier_name_unique", def = "{'supplier.$id': 1, 'name': 1}", unique = true)
public class Product {
    private String id;
    private String name;
    private String origin;
    @DBRef
    private User supplier;
    private String contactPersonName;
    private String contactPersonPhoneNumber;
    private boolean inStock;
    private Integer stockQuantity;
    @DBRef
    private Category category;
    @DBRef
    private SubCategory subCategory;
    private String image;

    private String unit;
    private LocalDate productionDate;
    private LocalDate expirationDate;

}
