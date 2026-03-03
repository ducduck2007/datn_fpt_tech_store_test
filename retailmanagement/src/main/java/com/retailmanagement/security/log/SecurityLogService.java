package com.retailmanagement.security.log;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SecurityLogService {

    private final SecurityLogRepository securityLogRepository;

    public void log(
            String username,
            ActionType actionType,
            String targetEntity,
            String targetId,
            String ip,
            SeverityLevel severity,
            String status,
            String description
    ) {

        SecurityLog log = SecurityLog.builder()
                .username(username)
                .actionType(actionType.name())
                .targetEntity(targetEntity)
                .targetId(targetId)
                .ipAddress(ip)
                .severity(severity.name())
                .status(status)
                .description(description)
                .createdAt(Instant.now())
                .build();

        securityLogRepository.save(log);
    }
}
