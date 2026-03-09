package com.retailmanagement.controller;

import com.retailmanagement.dto.request.ChangePasswordRequest;
import com.retailmanagement.dto.request.LoginRequest;
import com.retailmanagement.dto.request.RegisterRequest;
import com.retailmanagement.dto.response.ApiResponse;
import com.retailmanagement.dto.response.AuthResponse;
import com.retailmanagement.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        return ApiResponse.success("Đăng nhập thành công", authService.login(req,request));
    }

    @PostMapping("/logout")
    public ApiResponse<AuthResponse> logout() {
        authService.logout();
        return ApiResponse.success("Đăng xuất thành công", null);
    }
    // Thêm endpoint này vào AuthController.java

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.success("Đổi mật khẩu thành công", null);
    }
}
