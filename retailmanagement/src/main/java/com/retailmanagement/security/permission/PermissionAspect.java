package com.retailmanagement.security.permission;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;

    @Before("@annotation(checkPermission)")
    public void checkPermission(CheckPermission checkPermission) {

        String permission = checkPermission.value().name();

        if (!permissionService.hasPermission(permission)) {
            throw new AccessDeniedException("Forbidden");
        }
    }
}
