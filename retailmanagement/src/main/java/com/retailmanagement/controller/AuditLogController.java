package com.retailmanagement.controller;

import com.retailmanagement.audit.AuditLogService;
import com.retailmanagement.audit.AuditReportService;
import com.retailmanagement.dto.response.AuditLogResponse;
import com.retailmanagement.dto.response.ModuleLogReportResponse;
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
    private final AuditReportService reportService;

    @GetMapping("")
    public List<AuditLogResponse> getAllAuditLogs(){
        return auditLogService.findAll();
    }

    @GetMapping("filter/day")
    public List<AuditLogResponse> filterByDay(@RequestParam("day") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate date){
        return auditLogService.getAuditLogsByDate(date);
    }

    @GetMapping("filter/change-role")
    public List<AuditLogResponse> filterByChange_Role(){
        return  auditLogService.getAuditLogsByChange_Role();
    }

    @GetMapping("filter/user_id")
    public List<AuditLogResponse> filterByUser_Id(@RequestParam("id")Integer id){
        return auditLogService.getAuditLogsByUser_Id(id);
    }

    @GetMapping("report/module")
    public List<ModuleLogReportResponse> reportByModule(){
        return reportService.reportByModule();
    }
}
