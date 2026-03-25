package com.api.wishoria.security;

import com.api.wishoria.config.CacheConfig;
import com.api.wishoria.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@NullMarked
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final String EMAIL_NOT_FOUND_MESSAGE = "User with email: %s not found";

    private final UserRepository userRepository;

    @Override
    @Cacheable(value = CacheConfig.USERS_BY_EMAIL_CACHE, key = "#email.toLowerCase().trim()")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email.toLowerCase().trim();
        return userRepository.findByEmail(normalizedEmail).orElseThrow(() ->
                new UsernameNotFoundException(String.format(EMAIL_NOT_FOUND_MESSAGE, normalizedEmail)));
    }
}
