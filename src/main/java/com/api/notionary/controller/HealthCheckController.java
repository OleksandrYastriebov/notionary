package com.api.notionary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Health Check", description = "Service endpoint to maintain API activity")
@RestController
@RequestMapping("/api/v1/health")
public class HealthCheckController {

    @Operation(summary = "Server Health check state")
    @GetMapping
    public ResponseEntity<Map<String, String>> check() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "Notionary is running and feeling good!"
        ));
    }
}