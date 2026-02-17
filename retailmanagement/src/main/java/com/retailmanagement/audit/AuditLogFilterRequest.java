package com.retailmanagement.audit;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class AuditLogFilterRequest {
    private Long userId;

    private String module;
    private String action;

    private String targetType;
    private Long targetId;

    private String ipAddress;

    private String keyword;

    private Instant startDate;
    private Instant endDate;

    private List<String> modules;
    private List<String> actions;
}
