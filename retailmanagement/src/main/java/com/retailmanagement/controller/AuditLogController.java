package com.retailmanagement.controller;

import com.retailmanagement.audit.AuditLogService;
import com.retailmanagement.dto.response.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/auth/auditLogs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("")
    public List<AuditLogResponse> getAllAuditLogs(){
        return auditLogService.findAll();
    }
}
