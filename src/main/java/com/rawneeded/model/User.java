package com.rawneeded.model;


import com.rawneeded.enumeration.AccountStatus;
import com.rawneeded.enumeration.LanguagePreference;
import com.rawneeded.enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document
@Builder
public class User {

    private String id;
    private String name;
    private String fullName;
    private Role role;
    private String password;
    @Indexed(unique = true)
    private String email;
    @Indexed(unique = true)
    private String phoneNumber;
    private String forgetPasswordOTP;
    private AccountStatus accountStatus = AccountStatus.INACTIVE;
    private String profileImage;

    private LanguagePreference languagePreference = LanguagePreference.EN;
    
    // For staff members, this references the owner
    private String ownerId;
    
    @DBRef
    private SubscriptionPlan subscriptionPlan;
    
    @DBRef
    private Category category;

    @DBRef
    private List<SubCategory> subCategories;

    // List of allowed screen IDs (for staff members)
    private List<String> allowedScreens;

}
