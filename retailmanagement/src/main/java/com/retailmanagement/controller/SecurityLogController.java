package com.retailmanagement.controller;

import com.retailmanagement.security.log.SecurityLog;
import com.retailmanagement.security.log.SecurityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/auth/security-log")
@RequiredArgsConstructor
public class SecurityLogController {

    private final SecurityLogRepository securityLogRepository;

    @GetMapping
    public List<SecurityLog> getAll() {
        return securityLogRepository.findAll();
    }
}
