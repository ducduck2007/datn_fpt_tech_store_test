package com.retailmanagement.service;

import com.retailmanagement.dto.response.UserLoginResponse;
import com.retailmanagement.entity.User;
import com.retailmanagement.entity.UserLogin;
import com.retailmanagement.repository.UserLoginRepository;
import com.retailmanagement.util.IpUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLoginLogService {

    private final UserLoginRepository userLoginRepository;
    private final EntityManager entityManager;

    public void logLoginSuccess(
            Integer userId,
            String username,
            HttpServletRequest request
    ) {
        UserLogin log = new UserLogin();

        User userRef = entityManager.getReference(User.class, userId);

        log.setUser(userRef);
        log.setUsername(username);
        log.setSuccess(true);
        log.setIpAddress(IpUtil.getClientIp(request));
        log.setUserAgent(request.getHeader("User-agent"));

        userLoginRepository.save(log);
    }

    public void logLoginFail(
            String username,
            HttpServletRequest request
    ) {
        UserLogin log =  new UserLogin();

        log.setUsername(username);
        log.setSuccess(false);
        log.setIpAddress(IpUtil.getClientIp(request));
        log.setUserAgent(request.getHeader("User-agent"));

        userLoginRepository.save(log);
    }

    public void logLogout (Integer userId) {
        userLoginRepository
                .findTopByUser_IdAndSuccessTrueAndUpdatedAtIsNullOrderByCreatedAtDesc(userId)
                .ifPresent(log -> {
                    log.setUpdatedAt(Instant.now());
                    userLoginRepository.save(log);
                });
    }

    public List<UserLoginResponse> getAllUserLogins() {
        return userLoginRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserLoginResponse toResponse(UserLogin userLogin){
        Integer userId = userLogin.getUser() != null
                ? userLogin.getUser().getId()
                : null;

        return UserLoginResponse.builder()
                .id(userLogin.getId())
                .userId(userId)
                .username(userLogin.getUsername())
                .success(userLogin.getSuccess())
                .ipAddress(userLogin.getIpAddress())
                .userAgent(userLogin.getUserAgent())
                .createdAt(userLogin.getCreatedAt())
                .updatedAt(userLogin.getUpdatedAt())
                .build();
    }
}
