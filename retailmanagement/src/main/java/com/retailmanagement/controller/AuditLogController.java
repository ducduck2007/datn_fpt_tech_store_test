package com.retailmanagement.controller;

import com.retailmanagement.audit.AuditLogService;
import com.retailmanagement.dto.response.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/auth/audit-log")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("")
    public List<AuditLogResponse> getAllAuditLogs(){
        return auditLogService.findAll();
    }

    @GetMapping("filter")
    public List<AuditLogResponse> filterByDay(@RequestParam("day") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date){
        return auditLogService.getAuditLogsByDate(date);
    }
}
