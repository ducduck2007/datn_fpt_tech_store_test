package com.techstore.auth.dto;

public class LoginResponse {
    public String accessToken;

    public LoginResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
