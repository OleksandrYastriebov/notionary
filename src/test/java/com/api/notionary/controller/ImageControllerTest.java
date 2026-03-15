package com.api.notionary.controller;

import com.api.notionary.dto.image.ImageDto;
import com.api.notionary.exception.GlobalExceptionHandler;
import com.api.notionary.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageController Unit Tests")
class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void uploadImage_shouldReturnOkWithImageDto_whenFileIsValid() {
        String expectedImageUrl = "http://example.com/image.jpg";
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test-image.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

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
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty-image.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class, () -> imageController.uploadImage(emptyFile));

        assertEquals("Cannot upload empty file", thrown.getMessage());
        verify(imageService, never()).uploadImage(any(MultipartFile.class));
    }

    @Test
    void uploadImage_shouldHandleNullServiceResponse() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test-image.jpg", MediaType.IMAGE_JPEG_VALUE, "content".getBytes());

        when(imageService.uploadImage(any(MultipartFile.class))).thenReturn(null);

        ResponseEntity<ImageDto> responseEntity = imageController.uploadImage(mockFile);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertNull(responseEntity.getBody().url());

        verify(imageService).uploadImage(mockFile);
    }

    @Test
    void uploadImage_viaHttp_whenFileIsValid_shouldReturn200WithUrl() throws Exception {
        String expectedUrl = "https://res.cloudinary.com/example/image.jpg";
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, "image-bytes".getBytes());

        when(imageService.uploadImage(any())).thenReturn(expectedUrl);

        mockMvc.perform(multipart("/api/v1/images").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(expectedUrl));
    }

    /**
     * The controller throws IllegalArgumentException for empty files.
     * GlobalExceptionHandler routes IllegalArgumentException through the catch-all Throwable
     * handler which returns 500. The business logic validation (no empty file) is tested via
     * direct invocation above in uploadImage_shouldThrowIllegalArgumentException_whenFileIsEmpty.
     */
    @Test
    void uploadImage_viaHttp_whenFileIsEmpty_shouldReturn500() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

        mockMvc.perform(multipart("/api/v1/images").file(emptyFile))
                .andExpect(status().isInternalServerError());

        verify(imageService, never()).uploadImage(any());
    }
}
