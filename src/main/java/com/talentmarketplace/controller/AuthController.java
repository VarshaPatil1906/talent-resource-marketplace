package com.talentmarketplace.controller;

import com.talentmarketplace.dto.AuthDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto.LoginRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Persist context in HTTP Session
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            AuthDto.AuthResponse authResponse = buildAuthResponse(authentication);
            return ResponseEntity.ok(authResponse);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password", "status", 401));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated", "status", 401));
        }

        AuthDto.AuthResponse authResponse = buildAuthResponse(authentication);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    private AuthDto.AuthResponse buildAuthResponse(Authentication auth) {
        String username = auth.getName();
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        boolean isAdmin = roles.contains("ROLE_ADMIN");
        boolean isManager = roles.contains("ROLE_MANAGER");
        boolean isRecruiter = roles.contains("ROLE_RECRUITER");

        String activeRole = isAdmin ? "ADMIN" : (isManager ? "MANAGER" : (isRecruiter ? "RECRUITER" : "USER"));

        Map<String, Boolean> permissions = new LinkedHashMap<>();
        // Demand creation is strictly permitted for Recruiter
        permissions.put("canCreateDemand", isRecruiter);
        // Evaluating demands & running comparisons is permitted for Recruiter and Manager
        permissions.put("canEvaluateDemands", isRecruiter || isManager);
        permissions.put("canSearchResources", true);
        permissions.put("canCompareCandidates", isRecruiter || isManager);
        // Allocation request submission is permitted for Recruiter and Manager
        permissions.put("canRequestAllocation", isRecruiter || isManager);
        // Approval, rejection, and cancellation governance is strictly permitted for Manager
        permissions.put("canApproveAllocation", isManager);
        permissions.put("canRejectAllocation", isManager);
        permissions.put("canCancelAllocation", isManager);
        // Employee skill assessments can be recorded by Manager or Admin
        permissions.put("canAddEmployeeSkill", isManager || isAdmin);
        // Global skill taxonomy and synthetic employee provisioning is strictly Admin
        permissions.put("canManageSkills", isAdmin);
        permissions.put("canManageEmployees", isAdmin);
        // AI staffing assistant execution
        permissions.put("canExecuteAiAssistant", isRecruiter || isManager);

        return new AuthDto.AuthResponse(username, roles, activeRole, permissions);
    }
}
