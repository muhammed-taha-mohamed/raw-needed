package com.rawneeded.model;


import com.rawneeded.enummeration.Category;
import com.rawneeded.enummeration.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document
@Builder
public class User {
    private String id;
    private String name;
    private Role role;
    private String password;
    @Indexed(unique = true)
    private String email;
    @Indexed(unique = true)
    private String phoneNumber;
    private Category preferredCategory;
    private String forgetPasswordOTP;

}
