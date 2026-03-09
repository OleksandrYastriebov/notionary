package com.api.notionary.controller;

import com.api.notionary.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api")
public class EmailConfirmationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public EmailConfirmationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping(path = "/confirm-email")
    public String confirm(@RequestParam("token") String token, Model model) {
        try {
            return authenticationService.confirmToken(token);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "error";
        }
    }
}
