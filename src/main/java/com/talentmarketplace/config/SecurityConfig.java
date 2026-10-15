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

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

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
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(manager, recruiter, admin);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .securityContext(securityContext -> securityContext
                .securityContextRepository(securityContextRepository)
            )
            .authorizeHttpRequests(auth -> auth
                // Static assets & frontend UI
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/favicon.ico", "/h2-console/**").permitAll()
                // Auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Recruiter only: Project Demand Intake
                .requestMatchers(HttpMethod.POST, "/api/demands").hasRole("RECRUITER")
                // Admin only: Skill taxonomy and synthetic employee provisioning
                .requestMatchers(HttpMethod.POST, "/api/skills").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/employees").hasRole("ADMIN")
                // Manager & Admin: Employee skill assessment recording
                .requestMatchers(HttpMethod.POST, "/api/employees/*/skills").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/employees/{id}/skills").hasAnyRole("MANAGER", "ADMIN")
                // Recruiter & Manager: Allocation request submission
                .requestMatchers(HttpMethod.POST, "/api/allocations").hasAnyRole("RECRUITER", "MANAGER")
                // Manager only: Allocation approval, rejection, and cancellation governance
                .requestMatchers(HttpMethod.POST, "/api/allocations/*/approve").hasRole("MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/allocations/{id}/approve").hasRole("MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/allocations/*/reject").hasRole("MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/allocations/{id}/reject").hasRole("MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/allocations/*/cancel").hasRole("MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/allocations/{id}/cancel").hasRole("MANAGER")
                // Read & Query endpoints (demands, employees, skills, availability, reports, audit history)
                .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
