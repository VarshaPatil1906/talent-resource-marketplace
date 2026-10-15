package com.talentmarketplace.dto;

import java.util.List;
import java.util.Map;

public class AuthDto {

    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest() {}

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class AuthResponse {
        private String username;
        private List<String> roles;
        private String activeRole;
        private Map<String, Boolean> permissions;

        public AuthResponse() {}

        public AuthResponse(String username, List<String> roles, String activeRole, Map<String, Boolean> permissions) {
            this.username = username;
            this.roles = roles;
            this.activeRole = activeRole;
            this.permissions = permissions;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public String getActiveRole() {
            return activeRole;
        }

        public void setActiveRole(String activeRole) {
            this.activeRole = activeRole;
        }

        public Map<String, Boolean> getPermissions() {
            return permissions;
        }

        public void setPermissions(Map<String, Boolean> permissions) {
            this.permissions = permissions;
        }
    }
}
