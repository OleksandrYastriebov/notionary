package com.api.notionary.controller;

import com.api.notionary.dto.SignInRequestDto;
import com.api.notionary.dto.SignUpRequestDto;
import com.api.notionary.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserAuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public UserAuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(path= "/sign-up")
    public String singUp(@RequestBody SignUpRequestDto request) {
        return authenticationService.signUp(request);
    }

    @PostMapping(path= "/sign-in")
    public String singIn(@RequestBody SignInRequestDto request) {
        return authenticationService.signIn(request);
    }

    @GetMapping(path = "/confirm-email")
    public String confirm(@RequestParam("token") String token) {
        return authenticationService.confirmToken(token);
    }
}
