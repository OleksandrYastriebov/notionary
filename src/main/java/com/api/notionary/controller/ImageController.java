package com.api.notionary.controller;

import com.api.notionary.dto.image.ImageDto;
import com.api.notionary.security.interceptor.RateLimitPlan;
import com.api.notionary.security.interceptor.RateLimited;
import com.api.notionary.service.ImageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Images", description = "Image upload API")
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@RateLimited(action = RateLimitPlan.MUTATION)
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<ImageDto> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        String imageUrl = imageService.uploadImage(file);
        return ResponseEntity.ok(new ImageDto(imageUrl));
    }
}