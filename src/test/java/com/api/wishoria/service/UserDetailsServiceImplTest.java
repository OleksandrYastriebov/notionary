package com.api.wishoria.service;

import com.api.wishoria.entity.User;
import com.api.wishoria.entity.UserRole;
import com.api.wishoria.repository.UserRepository;
import com.api.wishoria.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        User user = new User("John", "Doe", "john@example.com", "encoded",
                Instant.now(), UserRole.ROLE_USER);
        user.setId(1L);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("john@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john@example.com");
        assertThat(result.getPassword()).isEqualTo("encoded");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void loadUserByUsername_shouldNormalizeEmail() {
        User user = new User("John", "Doe", "john@example.com", "encoded",
                Instant.now(), UserRole.ROLE_USER);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        userDetailsService.loadUserByUsername("  JOHN@Example.COM  ");

        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void loadUserByUsername_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@example.com");
    }
}
