package com.rawneeded.dto.user;


import com.rawneeded.enumeration.AccountStatus;
import com.rawneeded.enumeration.LanguagePreference;
import com.rawneeded.enumeration.Role;
import com.rawneeded.model.Category;
import com.rawneeded.model.SubCategory;
import com.rawneeded.model.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserRequestDto {

    private String name;
    private String fullName;
    private String phoneNumber;
    private String profileImage;

    private LanguagePreference languagePreference = LanguagePreference.EN;

    private String categoryId;

    private List<String> subCategoryIds;

    private List<String> allowedScreens;
}
