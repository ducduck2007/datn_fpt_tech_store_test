package com.retailmanagement.controller;

import com.retailmanagement.audit.AuditLog;
import com.retailmanagement.audit.AuditLogService;
import com.retailmanagement.audit.AuditReportService;
import com.retailmanagement.dto.response.AuditLogResponse;
import com.retailmanagement.dto.response.ModuleLogReportResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
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

    @GetMapping("export")
    public void exportAuditLogs(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition","attachment; filename=audit_logs.csv");

        List<AuditLog> logs = auditLogService.getByDateRange(from,to);

        PrintWriter writer = response.getWriter();

        writer.println(
                "id,user_id,module,action,target_type,target_id,details_json,ip_address,created_at"
        );

        for(AuditLog log: logs) {
            writer.printf(
                    "%d,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    log.getId(),
                    safe(log.getUser() != null ? log.getUser().getId() : ""),
                    log.getModule(),
                    log.getAction(),
                    log.getTargetType(),
                    log.getTargetId(),
                    safe(log.getDetailsJson()),
                    log.getIpAddress(),
                    log.getCreatedAt()
            );
        }
        writer.flush();
    }

    private String safe(Object value) {
        if (value == null) return "";
        String s = value.toString().replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
