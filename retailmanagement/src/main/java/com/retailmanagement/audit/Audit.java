package com.retailmanagement.audit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {

    String module();
    AuditAction action();
    String targetType();
}
