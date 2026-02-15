package com.community.config;

import com.community.handler.AccessDeniedHandlerImpl;
import com.community.handler.AuthenticationEntryPointImpl;
import com.community.handler.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Disable CSRF because we use JWTs for stateless authentication
        http.csrf(csrf -> csrf.disable());

        // Stateless session management: SecurityContext is built from JWT each request
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Centralized exception handling for 401/403
        http.exceptionHandling(ex -> ex
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler)
        );

        // Define which endpoints are public and which require authentication
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/health",
                "/api/common/avatar/**",
                "/api/auth/login",
                "/api/customer/register",
                "/error",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**"
            ).permitAll()
            .anyRequest().authenticated()
        );

        // Add JWT filter before the built-in username/password filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        // Expose AuthenticationManager so AuthService can authenticate credentials
        return configuration.getAuthenticationManager();
    }

}
