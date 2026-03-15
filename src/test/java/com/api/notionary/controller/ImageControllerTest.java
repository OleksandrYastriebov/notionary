package com.api.notionary.controller;

import com.api.notionary.dto.image.ImageDto;
import com.api.notionary.service.ImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageController Unit Tests")
class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    @Test
    void uploadImage_shouldReturnOkWithImageDto_whenFileIsValid() {
        String expectedImageUrl = "http://example.com/image.jpg";
        MultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(imageService.uploadImage(any(MultipartFile.class))).thenReturn(expectedImageUrl);

        ResponseEntity<ImageDto> responseEntity = imageController.uploadImage(mockFile);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(expectedImageUrl, responseEntity.getBody().url());

        verify(imageService).uploadImage(mockFile);
    }

    @Test
    void uploadImage_shouldThrowIllegalArgumentException_whenFileIsEmpty() {
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty-image.jpg",
                "image/jpeg",
                new byte[0]
        );

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> imageController.uploadImage(emptyFile));

        assertEquals("Cannot upload empty file", thrown.getMessage());
        verify(imageService, never()).uploadImage(any(MultipartFile.class));
    }

    @Test
    void uploadImage_shouldHandleNullServiceResponse() {
        MultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        when(imageService.uploadImage(any(MultipartFile.class))).thenReturn(null);

        ResponseEntity<ImageDto> responseEntity = imageController.uploadImage(mockFile);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertNull(responseEntity.getBody().url());

        verify(imageService).uploadImage(mockFile);
    }
}
