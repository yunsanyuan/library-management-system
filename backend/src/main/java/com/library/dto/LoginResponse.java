package com.library.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String role;
    private String username;
    private String avatarUrl;

    public LoginResponse(String token, String role, String username, String avatarUrl) {
        this.token = token;
        this.role = role;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }
}
