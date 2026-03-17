package com.api.wishoria.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private ImageService imageService;

    @Test
    void uploadImage_shouldReturnSecureUrl_whenUploadSucceeds() throws IOException {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/example/image.jpg"));

        String url = imageService.uploadImage(file);

        assertThat(url).isEqualTo("https://res.cloudinary.com/example/image.jpg");
    }

    @Test
    void uploadImage_shouldThrow_whenSecureUrlMissing() throws IOException {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of("other_key", "value"));

        assertThatThrownBy(() -> imageService.uploadImage(file))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("secure_url");
    }

    @Test
    void uploadImage_shouldThrow_whenIOException() throws IOException {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("read error"));
        when(cloudinary.uploader()).thenReturn(uploader);

        assertThatThrownBy(() -> imageService.uploadImage(file))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to upload image");
    }
}
