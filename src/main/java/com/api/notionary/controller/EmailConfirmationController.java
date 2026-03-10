package com.api.notionary.controller;

import com.api.notionary.dto.ApiResponse;
import com.api.notionary.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class EmailConfirmationController {

    private final AuthenticationService authenticationService;

    @GetMapping(path = "/confirm-email")
    public ResponseEntity<ApiResponse> confirmEmail(@RequestParam("token") String token) {
        try {
            ApiResponse result = authenticationService.confirmToken(token);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException ex) {
            log.error("Unexpected error during token confirmation. ", ex);
            return ResponseEntity.badRequest().body(new ApiResponse(ex.getMessage()));
        }
    }
}
