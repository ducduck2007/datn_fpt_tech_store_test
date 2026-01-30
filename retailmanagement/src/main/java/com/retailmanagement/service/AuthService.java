package com.retailmanagement.service;

import com.retailmanagement.dto.request.LoginRequest;
import com.retailmanagement.dto.request.RegisterRequest;
import com.retailmanagement.dto.response.AuthResponse;
import com.retailmanagement.dto.response.UserResponse;
import com.retailmanagement.entity.Customer;
import com.retailmanagement.entity.CustomerType;
import com.retailmanagement.entity.User;
import com.retailmanagement.repository.CustomRes;
import com.retailmanagement.repository.UserRepository;
import com.retailmanagement.security.JwtService;
import com.retailmanagement.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomRes customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final UserLoginLogService userLoginLogService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // 1) Create USER
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role("CUSTOMER")
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // 2) Create/ensure CUSTOMER with same email, is_active=true
        Optional<Customer> existingCus = customerRepository.findByEmail(req.getEmail());
        if (existingCus.isEmpty()) {
            Customer customer = Customer.builder()
                    .name(req.getUsername())
                    .email(req.getEmail())
                    .phone(null)
                    .dateOfBirth(null)
                    .customerType(CustomerType.REGULAR)
                    .vipTier(null)
                    .segmentsJson(null)
                    .loyaltyPoints(0)
                    .totalSpent(BigDecimal.ZERO)
                    .address(null)
                    .notes(null)
                    .isActive(true)
                    .lastLoginAt(LocalDateTime.now()) // ✅ Set initial login time
                    .build();

            customerRepository.save(customer);
        } else {
            Customer customer = existingCus.get();
            customer.setIsActive(true);
            if (customer.getName() == null || customer.getName().isBlank()) {
                customer.setName(req.getUsername());
            }
            if (customer.getEmail() == null || customer.getEmail().isBlank()) {
                customer.setEmail(req.getEmail());
            }
            customer.setLastLoginAt(LocalDateTime.now()); // ✅ Update login time
            customerRepository.save(customer);
        }

        String token = jwtService.generateToken(user);
        UserResponse userRes = modelMapper.map(user, UserResponse.class);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationMillis())
                .user(userRes)
                .build();
    }

    public AuthResponse login(LoginRequest req, HttpServletRequest request) {
        User user = userRepository.findByUsernameOrEmail(req.getIdentifier(), req.getIdentifier())
                .orElseThrow(() -> {
                    userLoginLogService.logLoginFail(
                            req.getIdentifier(),
                            request
                    );
                    return new RuntimeException("Sai tài khoản hoặc mật khẩu");
                });

        if (Boolean.FALSE.equals(user.getIsActive())) {
            userLoginLogService.logLoginFail(
                    user.getUsername(),
                    request
            );
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), req.getPassword())
        );

        if (!auth.isAuthenticated()) {
            userLoginLogService.logLoginFail(
                    user.getUsername(),
                    request
            );
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
        }

        userLoginLogService.logLoginSuccess(
                user.getId(),
                user.getUsername(),
                request
        );

        // ✅✅✅ THÊM CODE NÀY - Update last_login_at cho customer
        customerRepository.findByEmail(user.getEmail()).ifPresent(customer -> {
            customer.setLastLoginAt(LocalDateTime.now());
            customerRepository.save(customer);
            System.out.println("✅ Updated last_login_at for customer: " + user.getEmail());
        });

        String token = jwtService.generateToken(user);
        UserResponse userRes = modelMapper.map(user, UserResponse.class);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationMillis())
                .user(userRes)
                .build();
    }

    public void logout() {
        Integer userId = SecurityUtil.getCurrentUserId();
        if(userId != null) {
            userLoginLogService.logLogout(userId);
        }
    }
}