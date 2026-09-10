package com.juan.tfgplatform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/login",
                    "/registro",
                    "/recuperar-contrasena",
                    "/reset-contrasena",
                    "/confirmar-email",
                    "/api/usuarios",
                    "/css/**",
                    "/js/**",
                    "/uploads/**"
                ).permitAll()

                .requestMatchers("/api/ejercicios/**").hasRole("PROFESOR")
                .requestMatchers("/api/preguntas/**").hasRole("PROFESOR")
                .requestMatchers("/api/respuestas/**").hasRole("ALUMNO")
                .requestMatchers("/api/entregas/**").hasRole("ALUMNO")

                .anyRequest().authenticated()
            )

            .formLogin(login -> login
                .loginPage("/login")
                .successHandler(loginSuccessHandler())
                .permitAll()
            )

            .logout(logout -> logout
                .logoutSuccessUrl("/login")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler();
    }
}