package com.retailmanagement.audit;

import com.retailmanagement.dto.response.ModuleLogReportResponse;
import com.retailmanagement.dto.response.UserActionReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditReportService {

    private final AuditLogRepository auditLogRepository;

    public List<ModuleLogReportResponse> reportByModule(){
        return auditLogRepository.reportByModule()
                .stream()
                .map(r -> new ModuleLogReportResponse(
                        (String) r[0],
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    public List<UserActionReportResponse> getUserActionReport() {
        return auditLogRepository.countTotalActionByUser();
    }
}
