package com.rawneeded.controller;


import com.rawneeded.dto.ResponsePayload;
import com.rawneeded.service.impl.ImagesService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
}
