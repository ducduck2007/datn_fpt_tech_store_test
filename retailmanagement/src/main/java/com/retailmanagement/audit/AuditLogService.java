package com.retailmanagement.audit;

import com.retailmanagement.dto.response.AuditLogResponse;
import com.retailmanagement.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public void save(
            Integer userId,
            AuditModule module,
            AuditAction action,
            TargetType target,
            Long targetId,
            String detailsJson,
            String ipAddress
    ) {
        AuditLog log = new AuditLog();

        // Dùng entityMananger.getReference để tạo proxy User để gán userId
        // (proxy đại diện cho User thật)
       if(userId != null) {
            User userRef = entityManager.getReference(User.class,userId);
            log.setUser(userRef);
       }
        log.setModule(module.name());
        log.setAction(action.name());
        log.setTargetType(target.name());
        log.setTargetId(targetId);
        log.setDetailsJson(detailsJson);
        log.setIpAddress(ipAddress);

        auditLogRepository.save(log);
    }

    public List<AuditLogResponse> findAll(){
        return auditLogRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AuditLogResponse> getAuditLogsByDate(LocalDate date) {
        ZoneId zone = ZoneId.of("UTC");

        Instant from = date.atStartOfDay(zone).toInstant();

        Instant to = date.plusDays(30).atStartOfDay(zone).toInstant();

        return auditLogRepository.findByCreatedAtBetween(from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog auditLog){

        Integer userId = auditLog.getUser() != null
                ? auditLog.getUser().getId()
                : null;

        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(userId)
                .module(auditLog.getModule())
                .action(auditLog.getAction())
                .targetType(auditLog.getTargetType())
                .targetId(auditLog.getTargetId())
                .detailsJson(auditLog.getDetailsJson())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
