package com.rawneeded.service.impl;

import com.cloudinary.Cloudinary;
import com.rawneeded.error.exceptions.AbstractException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImagesService {


    private final Cloudinary cloudinary;


    public String uploadImage(MultipartFile file) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "folder", "raw-needed-app-images",
                            "resource_type", "image"
                    )
            );

            return result.get("secure_url").toString();

        }catch (AbstractException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new RuntimeException("Image upload failed");
        }
    }
}

