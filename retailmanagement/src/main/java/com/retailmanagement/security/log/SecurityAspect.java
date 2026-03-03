package com.retailmanagement.security.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAspect {

    private final SecurityLogService securityLogService;

    @Around("@annotation(sensitive)")
    public Object logSensitive(ProceedingJoinPoint joinPoint,
                               SensitiveOperation sensitive) throws Throwable {
        String username = SecurityContextHolder.getContext()
                .getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "UNKNOWN";

        String ip = "UNKNOWN";
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs != null) {
            ip = attrs.getRequest().getRemoteAddr();
        }

        String targetId = extractTargetId(joinPoint);

        SeverityLevel severity = sensitive.severity();
        String status = "SUCCESS";
        String description = sensitive.description();

        try {
            Object result = joinPoint.proceed();

            // CHANGE_ROLE thì kiểm tra quyền
            if (sensitive.action() == ActionType.ROLE_CHANGE) {

                boolean isAdmin = SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                //  Không phải admin mà đổi quyền
                if (!isAdmin) {
                    severity = SeverityLevel.CRITICAL;
                    status = "UNAUTHORIZED_CHANGE_ROLE";
                    description = "Unauthorized role change attempt";
                }

                //  Tự nâng quyền
                if (username.equals(targetId)) {
                    severity = SeverityLevel.CRITICAL;
                    status = "SELF_ROLE_ESCALATION";
                    description = "User attempted to change own role";
                }
            }

            securityLogService.log(
                    username,
                    sensitive.action(),
                    sensitive.entity(),
                    targetId,
                    ip,
                    severity,
                    status,
                    description
            );

            return result;

        } catch (Exception ex) {

            securityLogService.log(
                    username,
                    sensitive.action(),
                    sensitive.entity(),
                    targetId,
                    ip,
                    SeverityLevel.CRITICAL,
                    "FAILED",
                    ex.getMessage()
            );

            throw ex;
        }
    }

    private String extractTargetId(ProceedingJoinPoint joinPoint) {

        for (Object arg : joinPoint.getArgs()) {

            if (arg == null) continue;

            // Ưu tiên lấy id dạng số
            if (arg instanceof Long || arg instanceof Integer) {
                return String.valueOf(arg);
            }
        }

        return null;
    }
}
