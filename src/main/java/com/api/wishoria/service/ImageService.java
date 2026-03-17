package com.api.wishoria.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        try {
            Map<String, Object> uploadParams = generateUparamsMap();

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader()
                    .upload(file.getBytes(), uploadParams);

            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null) {
                throw new IllegalStateException("Cloudinary did not return a secure_url");
            }

            return secureUrl.toString();

        } catch (IOException ex) {
            log.error("Failed to upload image to Cloudinary", ex);
            throw new IllegalStateException("Failed to upload image. Please try again later.", ex);
        }
    }

    private @NonNull Map<String, Object> generateUparamsMap() {
        return Map.of(
                "folder", "wishoria-wishlists",
                "fetch_format", "webp",
                "quality", "auto"
        );
    }
}