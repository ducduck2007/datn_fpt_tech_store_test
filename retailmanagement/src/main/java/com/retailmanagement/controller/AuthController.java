package com.retailmanagement.controller;

import com.retailmanagement.dto.request.LoginRequest;
import com.retailmanagement.dto.request.RegisterRequest;
import com.retailmanagement.dto.response.ApiResponse;
import com.retailmanagement.dto.response.AuthResponse;
import com.retailmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Đăng ký thành công", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Đăng nhập thành công", authService.login(request));
    }
}
