package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF for import endpoints (so curl works without token)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/clients/import",
                                "/credit-limits/import",
                                "/import/**",
                                "/upload-file"
                        )
                )
                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public pages
                        .requestMatchers("/login", "/css/**").permitAll()
                        // Make all import endpoints publicly accessible
                        .requestMatchers(
                                "/clients/import",
                                "/credit-limits/import",
                                "/import/**",
                                "/upload-file"
                        ).permitAll()
                        // Everything else requires login
                        .anyRequest().authenticated()
                )
                // Form login
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/authenticateTheUser")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                // Logout
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
