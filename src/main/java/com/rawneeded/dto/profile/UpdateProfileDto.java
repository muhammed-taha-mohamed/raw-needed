package com.rawneeded.dto.profile;

import com.rawneeded.enumeration.LanguagePreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateProfileDto {
    private String fullName;
    private MultipartFile profileImage;
    private String password;
    private LanguagePreference languagePreference;
}

