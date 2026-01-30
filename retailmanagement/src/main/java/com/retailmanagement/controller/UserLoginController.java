package com.retailmanagement.controller;

import com.retailmanagement.dto.response.UserLoginResponse;
import com.retailmanagement.entity.UserLogin;
import com.retailmanagement.service.UserLoginLogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/auth/userLogins")
@RequiredArgsConstructor
public class UserLoginController {

    private final UserLoginLogService userLoginLogService;

    @GetMapping("")
    public List<UserLoginResponse> findAll() {
        return userLoginLogService.getAllUserLogins();
    }

    @GetMapping("export")
    public void exportUserLogin(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            HttpServletResponse response
    ) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=login_logs.csv");

        List<UserLogin> logs = userLoginLogService.getByDateRange(from,to);

        PrintWriter writer = response.getWriter();

        writer.println("id,user_id,username,success,ip_address,user_agent,create_at,update_at");

        for (UserLogin log: logs) {
            writer.printf(
                    "%d,%s,%s,%s,%s,%s,%s,%s%n",
                    log.getId(),
                    safe(log.getUser() != null ? log.getUser().getId() : ""),
                    safe(log.getUser() != null ? log.getUser().getUsername() : ""),
                    log.getSuccess(),
                    log.getIpAddress(),
                    safe(log.getUserAgent()),
                    log.getCreatedAt(),
                    log.getUpdatedAt()
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
