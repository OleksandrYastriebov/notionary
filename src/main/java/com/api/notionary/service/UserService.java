package com.api.notionary.service;

import com.api.notionary.dto.UserDto;
import com.api.notionary.entity.ConfirmationToken;
import com.api.notionary.entity.User;
import com.api.notionary.exception.UserNotFoundException;
import com.api.notionary.helper.Mapper;
import com.api.notionary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenConfirmationService tokenConfirmationService;
    private final Mapper mapper;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException(String.format("User with email %s can not be found.", email)));
    }

    public UserDto findUserById(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id %s can not be found.", id)));
        return mapper.mapUserToDto(user);
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

    public int enableAppUser(String email) {
        return userRepository.enableAppUser(email);
    }

    public boolean isUserEnabled(String email) {
        return userRepository.findEnabledByEmail(email).orElseThrow(() ->
                new IllegalStateException("No 'enabled' flag found"));
    }

    public void deleteUserById(Long id) {
        if (findUserById(id) == null) {
            throw new UserNotFoundException(
                    String.format("*ERROR* Trying to delete user with id %s. User not found", id));
        }
        userRepository.deleteById(id);
        LOGGER.info("User with id {} was removed from repository.", id);
    }

    public List<UserDto> getAllDisabledUsers() {
        List<User> disabledUsers = userRepository.findUsersWithExpiredConfirmationTokenAndDisabled();
        return disabledUsers.stream()
                .map(mapper::mapUserToDto)
                .toList();
    }

    public void deleteUsers(List<UserDto> disabledUsers) {
        disabledUsers.stream()
                .map(UserDto::getId)
                .forEach(this::deleteUserById);
    }
}
