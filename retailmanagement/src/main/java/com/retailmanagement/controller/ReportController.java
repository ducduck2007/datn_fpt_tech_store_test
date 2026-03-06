package com.retailmanagement.controller;

import com.retailmanagement.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/revenue-by-channel")
    public ResponseEntity<?> revenueByChannel(){
        return ResponseEntity.ok(
                reportService.revenueByChannel()
        );
    }

    @GetMapping("/revenue-by-date")
    public ResponseEntity<?> revenueByDate(){
        return ResponseEntity.ok(
                reportService.revenueByDate()
        );
    }

    @GetMapping("/orders-by-staff")
    public ResponseEntity<?> ordersByStaff(){
        return ResponseEntity.ok(
                reportService.ordersByStaff()
        );
    }
}