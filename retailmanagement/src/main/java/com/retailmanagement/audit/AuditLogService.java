package com.retailmanagement.audit;

import com.retailmanagement.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
