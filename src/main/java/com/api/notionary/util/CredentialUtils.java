package com.api.notionary.util;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

public class CredentialUtils {
    private CredentialUtils() {
        throw new UnsupportedOperationException("Utility class and cannot be instantiated");
    }

    public static String getAuthenticatedUserEmail(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails) || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to be authorised to perform this action.");
        }
        return userDetails.getUsername();
    }
}
