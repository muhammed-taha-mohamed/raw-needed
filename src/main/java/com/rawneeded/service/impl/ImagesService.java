package com.rawneeded.service.impl;

import com.rawneeded.error.exceptions.AbstractException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class ImagesService {

    @Value("${app.images.upload-dir:images}")
    private String uploadDir;

    public String uploadImage(MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename).toAbsolutePath().normalize();
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Get base URL dynamically from the request
            String baseUrl = getBaseUrl();
            String imageUrl = baseUrl + "/api/v1/image/" + filename;
            log.info("Image uploaded successfully: {}", imageUrl);
            return imageUrl;

        } catch (AbstractException e) {
            throw e;
        } catch (IOException e) {
            log.error("Failed to upload image to server", e);
            throw new RuntimeException("Image upload failed", e);
        } catch (Exception e) {
            log.error("Failed to upload image", e);
            throw new RuntimeException("Image upload failed", e);
        }
    }

    private String getBaseUrl() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String scheme = request.getScheme(); // http or https
                String serverName = request.getServerName(); // localhost or api.rawneeded.com
                int serverPort = request.getServerPort();
                String contextPath = request.getContextPath(); // /raw-needed

                // Build base URL
                StringBuilder baseUrl = new StringBuilder();
                baseUrl.append(scheme).append("://").append(serverName);
                
                // Add port only if it's not the default port (80 for http, 443 for https)
                if ((scheme.equals("http") && serverPort != 80) || 
                    (scheme.equals("https") && serverPort != 443)) {
                    baseUrl.append(":").append(serverPort);
                }
                
                baseUrl.append(contextPath);
                
                return baseUrl.toString();
            }
        } catch (Exception e) {
            log.warn("Could not get base URL from request, using default", e);
        }
        
        // Fallback to default if request is not available
        return "https://api.rawneeded.com/raw-needed";
    }

    public Resource getImage(String filename) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).toAbsolutePath().normalize();
            
            // Security check: ensure the file is within the upload directory
            if (!filePath.startsWith(uploadPath)) {
                log.warn("Attempted to access file outside upload directory: {} (resolved path: {}, upload path: {})", 
                        filename, filePath, uploadPath);
                return null;
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                log.warn("Image file not found or not readable: {} (path: {})", filename, filePath);
                return null;
            }
        } catch (Exception e) {
            log.error("Error loading image: {}", filename, e);
            return null;
        }
    }
}

