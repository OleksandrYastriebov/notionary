package com.api.wishoria.config.security;

import com.api.wishoria.security.JwtAuthenticationFilter;
import com.api.wishoria.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/v1/resend-confirmation-email",
                                "/api/v1/confirm-email/**",
                                "/api/v1/refresh-token/**",
                                "/api/v1/sign-up/**",
                                "/api/v1/sign-in/**",
                                "/api/v1/seo/**",
                                "/api/v1/health",
                                "/css/**",
                                "/js/**").permitAll()
                        .requestMatchers("/api/v1/wishlists/*/access", "/api/v1/wishlists/*/access/**").authenticated()
                        .requestMatchers("/api/v1/forgot-password", "/api/v1/forgot-password/").authenticated()
                        .requestMatchers("/api/v1/reset-password", "/api/v1/reset-password/").authenticated()
                        .requestMatchers("/api/v1/ai/wishlists/*/generate-description", "/api/v1/ai/wishlists/(*)/generate-description/").authenticated()
                        .requestMatchers("/api/v1/ai/wishlists/generate-wishlists", "/api/v1/ai/wishlists/generate-wishlists").authenticated()
                        .requestMatchers("/api/v1/profiles/search", "/api/v1/profiles/search/").authenticated()
                        .requestMatchers("/api/v1/profiles/search/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/wishlists", "/api/v1/wishlists/").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/wishlists/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/wishlists/*/wishes/*/checked").permitAll()
                        .requestMatchers(
                                "/api/v1/wishlists/**", "/api/v1/user/**",
                                "/api/v1/sign-out/**").authenticated()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DEVELOPER")
                        .anyRequest().authenticated()
                ).exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"message\": \"Unauthorized: Authorization Token is missing or invalid.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"message\": \"Access Denied: You don't have enough permissions.\"}");
                        }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}
