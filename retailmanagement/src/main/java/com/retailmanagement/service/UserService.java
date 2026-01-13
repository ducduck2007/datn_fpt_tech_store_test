package com.retailmanagement.service;

import com.retailmanagement.dto.request.CreateUserRequest;
import com.retailmanagement.dto.request.UpdateUserRequest;
import com.retailmanagement.dto.response.UserResponse;
import com.retailmanagement.entity.User;
import com.retailmanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(CreateUserRequest request){

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại trong hệ thống");
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole()) // Role có thể để trống để trở thành CUSTOMER
                .build();

        //Lưu User lúc Create để hiện thị kiểm tra data lúc Create
        User savedUser = userRepository.save(user);

        //Trả User về dạng response để hiện thị
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .isActive(savedUser.getIsActive())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }

    public List<UserResponse> findAll() {
        //Dùng Builder ở trong UserResponse để lấy danh sách User
        return userRepository.findAll()
                .stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .isActive(user.getIsActive())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .build()
                )
                .toList();
    }



    public UserResponse updateUser(UpdateUserRequest request, Integer id){

        //Tìm user bằng id và trả về lỗi nếu không tìm thấy
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
            throw new RuntimeException("Username đã tồn tại trong hệ thống");
        }
        if(userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        //Lưu User được Update để hiện thị kiểm tra data lúc Update
        User savedUser = userRepository.save(user);

        //Trả User về dạng response để hiện thị
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .isActive(savedUser.getIsActive())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }

    public UserResponse deleteUser(Integer id) {

        //Tìm user bằng id và trả về lỗi nếu không tìm thấy
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        user.setIsActive(false);

        //Lưu User được Delete để hiện thị kiểm tra data lúc Delete
        User savedUser = userRepository.save(user);

        //Trả User về dạng response để hiện thị
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .isActive(savedUser.getIsActive())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();
    }
}
