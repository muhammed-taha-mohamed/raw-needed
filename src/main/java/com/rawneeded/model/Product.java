package com.rawneeded.model;


import com.rawneeded.enummeration.Category;
import com.rawneeded.enummeration.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document
@Builder
public class Product {
    private String id;
    private String name;
    private String origin;
    @DBRef
    private User supplier;
    private String contactPersonName;
    private String contactPersonPhoneNumber;
    private boolean inStock;
    private Category category;
    private SubCategory subCategory;

}
