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
        config.put("cloud_name", "dsan8cfe0");
        config.put("api_key","543723575351819");
        config.put("api_secret", "P4a2q0E9NkwF14RPxXrOb-PulRw");
        return new Cloudinary(config);
    }
}
