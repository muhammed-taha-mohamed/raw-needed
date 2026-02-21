package com.rawneeded.controller;


import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.service.impl.ImagesService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("api/v1/image")
@AllArgsConstructor
public class ImageUploadController {

    private final ImagesService imagesService;
    private final MessagesUtil messagesUtil;


    @PostMapping("/upload")
    public ResponseEntity<ResponsePayload> uploadImage(@RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(ResponsePayload.builder()
                .date(LocalDateTime.now())
                .content(Map.of(
                        "message", "Image uploaded successfully",
                        "data", imagesService.uploadImage(file)))
                .build()
        );

    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Resource resource = imagesService.getImage(filename);
        
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Determine content type
        String contentType = "application/octet-stream";
        try {
            String originalFilename = resource.getFilename();
            if (originalFilename != null) {
                if (originalFilename.toLowerCase().endsWith(".png")) {
                    contentType = MediaType.IMAGE_PNG_VALUE;
                } else if (originalFilename.toLowerCase().endsWith(".jpg") || 
                          originalFilename.toLowerCase().endsWith(".jpeg")) {
                    contentType = MediaType.IMAGE_JPEG_VALUE;
                } else if (originalFilename.toLowerCase().endsWith(".gif")) {
                    contentType = MediaType.IMAGE_GIF_VALUE;
                } else if (originalFilename.toLowerCase().endsWith(".webp")) {
                    contentType = "image/webp";
                }
            }
        } catch (Exception e) {
            // Use default content type
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
