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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomRes customerRepository; // ✅ added
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;

    // ✅ Requirement #3: customer registration must also insert into `customers`
    // Ensure atomicity: either both user+customer are saved or none.
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
                    .name(req.getUsername())            // default name from username
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
                    .build();

            customerRepository.save(customer);
        } else {
            // If already exists (rare, but safe), ensure active & data consistent
            Customer customer = existingCus.get();
            customer.setIsActive(true);
            if (customer.getName() == null || customer.getName().isBlank()) {
                customer.setName(req.getUsername());
            }
            // Keep email the same (requirement). If somehow null, set it.
            if (customer.getEmail() == null || customer.getEmail().isBlank()) {
                customer.setEmail(req.getEmail());
            }
            customerRepository.save(customer);
        }

        // 3) Return auth response as before
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
