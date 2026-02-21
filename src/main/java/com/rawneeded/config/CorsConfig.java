/*
 * Copyright (c) 2024. Mohammed Taha
 */

package com.rawneeded.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE" ,"PATCH" , "OPTIONS ")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve images from the images directory
        Path imagesPath = Paths.get("images").toAbsolutePath();
        String imagesLocation = imagesPath.toUri().toString();
        if (!imagesLocation.endsWith("/")) {
            imagesLocation += "/";
        }
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imagesLocation);
    }

}
