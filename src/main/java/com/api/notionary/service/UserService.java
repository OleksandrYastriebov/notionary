package com.api.notionary.service;

import com.api.notionary.entity.ConfirmationToken;
import com.api.notionary.entity.User;
import com.api.notionary.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class UserService implements UserDetailsService {
    private static final String EMAIL_NOT_FOUND_MESSAGE = "User with email: %s not found";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenConfirmationService tokenConfirmationService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       TokenConfirmationService tokenConfirmationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenConfirmationService = tokenConfirmationService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException(String.format(EMAIL_NOT_FOUND_MESSAGE, email)));
    }

    public String signUpUser(User user) {
        boolean userExists = userRepository.findByEmail(user.getEmail()).isPresent();
        if (userExists) {
            throw new IllegalStateException("Email already taken");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());

        user.setPassword(encodedPassword);

        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                user
        );
        tokenConfirmationService.saveConfirmationToken(confirmationToken);
        return token;
    }

    public UserDetailsService userDetailsService() {
        return this::loadUserByUsername;
    }

    public int enableAppUser(String email) {
        return userRepository.enableAppUser(email);
    }

    public boolean isUserEnabled(String email) {
        return userRepository.findEnabledByEmail(email).orElseThrow(() ->
                new IllegalStateException("No 'enabled' flag found"));
    }
}
