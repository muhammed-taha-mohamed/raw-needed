package com.rawneeded.dto.user;


import com.rawneeded.enumeration.LanguagePreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
