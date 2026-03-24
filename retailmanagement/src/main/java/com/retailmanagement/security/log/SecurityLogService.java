package com.retailmanagement.security.log;

import com.retailmanagement.repository.UserRepository;
import com.retailmanagement.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SecurityLogService {

    private final SecurityLogRepository securityLogRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private final Map<String, Long> lastAlertMap = new ConcurrentHashMap<>();

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
        try {
            SecurityLog log = SecurityLog.builder()
                    .username(username)
                    .actionType(actionType != null ? actionType.name() : null)
                    .targetEntity(targetEntity)
                    .targetId(targetId)
                    .ipAddress(ip)
                    .severity(severity != null ? severity.name() : null)
                    .status(status)
                    .description(description)
                    .build();

            securityLogRepository.save(log);

            // CRITICAL → gửi ngay
            if (severity == SeverityLevel.CRITICAL) {
                handleCriticalAlert(log);
            }

            // HIGH → check threshold
            if (severity == SeverityLevel.HIGH) {
                handleHighThreshold(log);
            }

        } catch (Exception ex) {
            // Không throw ra ngoài để tránh ảnh hưởng business flow
        }
    }

    //Helper
    private List<String> getAdminEmails() {
        return userRepository.findAdminEmails().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    //Xử lý alert
    private void handleCriticalAlert(SecurityLog log) {

        List<String> adminEmails = getAdminEmails();

        if (adminEmails.isEmpty()) return;

        String subject = "CRITICAL SECURITY ALERT - " + log.getActionType();

        String content = buildSecurityHtml(log);

        emailService.sendSecurityAlertToAdmins(
                adminEmails,
                subject,
                content
        );
    }

    private void handleHighThreshold(SecurityLog log) {

        long count = securityLogRepository.countRecentHigh(log.getActionType());

        if (count < 10) return;

        String key = log.getActionType() + "_" + log.getIpAddress();

        if (!shouldSendHighAlert(key)) return;

        List<String> adminEmails = getAdminEmails();

        if (adminEmails.isEmpty()) return;

        String subject = "HIGH ALERT - " + log.getActionType();

        String content = buildHighAlertHtml(log, count);

        emailService.sendSecurityAlertToAdmins(
                adminEmails,
                subject,
                content
        );
    }

    private boolean shouldSendHighAlert(String key) {

        long now = System.currentTimeMillis();

        Long lastSent = lastAlertMap.get(key);

        if (lastSent != null && now - lastSent < 300000) {
            return false;
        }

        lastAlertMap.put(key, now);
        return true;
    }

    //Build HTML
    private String buildSecurityHtml(SecurityLog log) {

        String formattedTime = log.getCreatedAt() != null
                ? log.getCreatedAt()
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
                : "N/A";

        return """
        <div style="font-family:Arial;padding:20px">
            <h2 style="color:red">🚨 SECURITY ALERT</h2>

            <p><b>User:</b> %s</p>
            <p><b>Action:</b> %s</p>
            <p><b>Target:</b> %s (%s)</p>
            <p><b>IP:</b> %s</p>
            <p><b>Status:</b> %s</p>
            <p><b>Description:</b> %s</p>
            <p><b>Time:</b> %s</p>

        </div>
    """.formatted(
                log.getUsername(),
                log.getActionType(),
                log.getTargetEntity(),
                log.getTargetId(),
                log.getIpAddress(),
                log.getStatus(),
                log.getDescription(),
                formattedTime
        );
    }

    private String buildHighAlertHtml(SecurityLog log, long count) {

        String time = log.getCreatedAt() != null
                ? log.getCreatedAt()
                .atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
                : "N/A";

        return """
        <div style="font-family:Arial;padding:20px">
            <h2 style="color:orange">⚠️ HIGH ALERT (Threshold Reached)</h2>
            
            <p><b>User:</b> %s</p>
            <p><b>Action:</b> %s</p>
            <p><b>Số lần:</b> %d lần trong 5 phút</p>
            <p><b>IP:</b> %s</p>
            <p><b>Time:</b> %s</p>

            <hr/>
            <p style="font-size:12px;color:gray">
                Có dấu hiệu tấn công hoặc hành vi bất thường
            </p>
        </div>
    """.formatted(
                log.getUsername(),
                log.getActionType(),
                count,
                log.getIpAddress(),
                time
        );
    }

    public Page<SecurityLog> filterLogs(
            SecurityLogFilterRequest request,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {

        // ===== 1. Giới hạn page size =====
        size = Math.min(size, 100); // max 100 record / page

        // ===== 2. Whitelist field được sort =====
        List<String> allowedSortFields = List.of(
                "createdAt",
                "username",
                "actionType",
                "severity",
                "status",
                "ipAddress"
        );

        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "createdAt";
        }

        // ===== 3. Validate sort direction =====
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDir);
        } catch (Exception e) {
            direction = Sort.Direction.DESC;
        }

        // ===== 4. Build Pageable =====
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                size,
                Sort.by(direction, sortBy)
        );

        // ===== 5. Specification =====
        Specification<SecurityLog> spec =
                Specification.where(SecurityLogSpecification.filter(request));

        return securityLogRepository.findAll(spec, pageable);
    }
}
