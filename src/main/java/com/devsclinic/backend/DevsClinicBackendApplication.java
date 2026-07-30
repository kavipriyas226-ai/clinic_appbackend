package com.devsclinic.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// UserDetailsServiceAutoConfiguration is excluded because auth is handled entirely by
// our own JwtAuthFilter/AuthService against the single Account document — Spring
// Security's default in-memory user bean would otherwise sit unused and just log a
// generated password at every startup.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class DevsClinicBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevsClinicBackendApplication.class, args);
    }
}
