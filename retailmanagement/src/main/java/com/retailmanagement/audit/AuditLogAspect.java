package com.retailmanagement.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.retailmanagement.dto.response.UserResponse;
import com.retailmanagement.util.IpUtil;
import com.retailmanagement.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    //Chỉ chạy khi Method chạy thành công (Ví dụ method: method Create, method Update)
    @AfterReturning(
            // Chỉ chạy khi có annotation audit (@Audit)
            pointcut = "@annotation(audit)",
            // Lấy dữ liệu của method (Ví dụ method: method Create, method Update)
            returning = "result"
    )
    public void logAfterSuccess(
            JoinPoint joinPoint,
            Audit audit,
            Object result
    ) throws JsonProcessingException {
        // Lấy id của user thao tác
        Integer userId = SecurityUtil.getCurrentUserId();

        // Lấy ip của user thao tác
        String ip = IpUtil.getClientIp(request);

        // Lấy id của đối tượng tác động (Ví dụ đối tượng: User, Customer, Product,...)
        Long targetId = extractTargetId(result);

        //
        String detailsJson = objectMapper.writeValueAsString(
                Map.of(
                        "method", joinPoint.getSignature().getName(),
                        "args", joinPoint.getArgs()
                )
        );

        // Lưu
        auditLogService.save(
                userId,
                audit.module(),
                audit.action(),
                audit.targetType(),
                targetId,
                detailsJson,
                ip
        );
    }

    //Hàm lấy id của đối tượng bị thao tác
    private Long extractTargetId(Object result) {
        if(result == null) {
            return null;
        }

        if(result instanceof UserResponse user) {
            return Long.valueOf(user.getId());
        }
        return null;
    }
}
