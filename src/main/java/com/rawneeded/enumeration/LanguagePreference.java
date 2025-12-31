package com.rawneeded.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LanguagePreference {
    EN("en", "English"),
    AR("ar", "Arabic");

    private final String code;
    private final String displayName;
}

