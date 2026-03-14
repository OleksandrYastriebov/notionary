package com.api.notionary.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class HealthCheckControllerTest {

    @InjectMocks
    private HealthCheckController healthCheckController;

    @Test
    void check_shouldReturnOkWithUpStatusAndMessage() {
        ResponseEntity<Map<String, String>> responseEntity = healthCheckController.check();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        Map<String, String> body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals("UP", body.get("status"));
        assertEquals("Notionary is running and feeling good!", body.get("message"));
        assertEquals(2, body.size());
    }

    @Test
    void check_shouldReturnExpectedMap() {
        ResponseEntity<Map<String, String>> responseEntity = healthCheckController.check();

        Map<String, String> expectedBody = Map.of(
                "status", "UP",
                "message", "Notionary is running and feeling good!"
        );

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedBody, responseEntity.getBody());
    }
}
