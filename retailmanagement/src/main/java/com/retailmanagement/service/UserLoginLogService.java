package com.retailmanagement.service;

import com.retailmanagement.dto.response.UserLoginResponse;
import com.retailmanagement.entity.User;
import com.retailmanagement.entity.UserLogin;
import com.retailmanagement.repository.UserLoginRepository;
import com.retailmanagement.repository.UserRepository;
import com.retailmanagement.security.log.ActionType;
import com.retailmanagement.security.log.SecurityLogService;
import com.retailmanagement.security.log.SeverityLevel;
import com.retailmanagement.util.IpUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserLoginLogService {

    private final UserLoginRepository userLoginRepository;
    private final EntityManager entityManager;
    private final SecurityLogService securityLogService;
    private final UserRepository userRepository;

    private final Map<String, Integer> failedAttempts =
            new ConcurrentHashMap<>();

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

        failedAttempts.remove(username);
    }

    public void logLoginFail(
            String username,
            HttpServletRequest request
    ) {
        UserLogin log = new UserLogin();

        log.setUsername(username);
        log.setSuccess(false);
        log.setIpAddress(IpUtil.getClientIp(request));
        log.setUserAgent(request.getHeader("User-agent"));

        userLoginRepository.save(log);

        int attempts = failedAttempts.getOrDefault(username, 0) + 1;
        failedAttempts.put(username, attempts);

        if (attempts == 5) {

            securityLogService.log(
                    username,
                    ActionType.INTRUSION_ATTEMPT,
                    "LOGIN",
                    null,
                    IpUtil.getClientIp(request),
                    SeverityLevel.CRITICAL,
                    "SUSPICIOUS",
                    "Multiple failed login attempts"
            );
        }
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

    public List<UserLogin> getByDateRange (LocalDate from, LocalDate to) {
        if(from == null && to == null){
            to = LocalDate.now();
            from = to.minusDays(7);
        }

        if(to == null){
            to = from;
        }

        if(from == null){
            from = to;
        }

        Instant fromInstant = from.atStartOfDay(ZoneId.systemDefault()).toInstant();

        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return userLoginRepository.findByCreatedAtBetween(fromInstant, toInstant);
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
