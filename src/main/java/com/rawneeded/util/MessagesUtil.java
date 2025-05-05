package com.rawneeded.util;


import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
@AllArgsConstructor
public class MessagesUtil {
    private final HttpServletRequest servletRequest;
    private final MessageSource messageSource;

    public String getMessage(String code) {
        String lang = getLangHeader()!=null && getLangHeader().equalsIgnoreCase("ar") ? "ar" : "en";
        Locale locale = new Locale(lang);
        return messageSource.getMessage(code, null,locale
        );
    }

    public String getLangHeader() {
        return servletRequest.getHeader("Accept-Language");
    }

    public String getAuthToken() {
        String bearerToken = servletRequest.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken;
    }

}
