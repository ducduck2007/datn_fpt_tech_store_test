package com.retailmanagement.service;

import com.retailmanagement.dto.request.LoginRequest;
import com.retailmanagement.dto.request.RegisterRequest;
import com.retailmanagement.dto.response.AuthResponse;
import com.retailmanagement.dto.response.UserResponse;
import com.retailmanagement.entity.User;
import com.retailmanagement.repository.UserRepository;
import com.retailmanagement.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role("CUSTOMER") // ✅ fix cứng role khi đăng ký
                .isActive(true)
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(user);
        UserResponse userRes = modelMapper.map(user, UserResponse.class);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationMillis())
                .user(userRes)
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsernameOrEmail(req.getIdentifier(), req.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Sai tài khoản hoặc mật khẩu"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), req.getPassword())
        );

        if (!auth.isAuthenticated()) {
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
        }

        String token = jwtService.generateToken(user);
        UserResponse userRes = modelMapper.map(user, UserResponse.class);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationMillis())
                .user(userRes)
                .build();
    }
}
