package com.rawneeded.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "drzge8ywz");
        config.put("api_key","424482221818672");
        config.put("api_secret", "aowzA7TI9wZoIzFW8ZIWxoIByuc");
        return new Cloudinary(config);
    }
}
