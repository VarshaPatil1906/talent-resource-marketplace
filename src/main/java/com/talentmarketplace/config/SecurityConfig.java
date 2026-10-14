package com.talentmarketplace.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.security.manager-password:password123}")
    private String managerPassword;

    @Value("${app.security.recruiter-password:password123}")
    private String recruiterPassword;

    @Value("${app.security.admin-password:password123}")
    private String adminPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails manager = User.builder()
                .username("manager")
                .password(encoder.encode(managerPassword))
                .roles("MANAGER")
                .build();

        UserDetails recruiter = User.builder()
                .username("recruiter")
                .password(encoder.encode(recruiterPassword))
                .roles("RECRUITER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode(adminPassword))
                .roles("ADMIN", "MANAGER")
                .build();

        return new InMemoryUserDetailsManager(manager, recruiter, admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // Static assets & frontend UI
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/favicon.ico", "/h2-console/**").permitAll()
                // Manager-only protected allocation approval, rejection and cancellation
                .requestMatchers(HttpMethod.POST, "/api/allocations/{id}/approve").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/allocations/{id}/reject").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/allocations/{id}/cancel").hasAnyRole("MANAGER", "ADMIN")
                // All other API read & request submission endpoints are open for recruiter/browser
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
