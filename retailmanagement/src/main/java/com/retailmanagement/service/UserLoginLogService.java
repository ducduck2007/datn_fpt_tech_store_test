package com.retailmanagement.service;

import com.retailmanagement.entity.User;
import com.retailmanagement.entity.UserLogin;
import com.retailmanagement.repository.UserLoginRepository;
import com.retailmanagement.util.IpUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

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
}
