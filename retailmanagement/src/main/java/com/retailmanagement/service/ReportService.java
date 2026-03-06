package com.retailmanagement.service;

import com.retailmanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;

    public List<Map<String,Object>> revenueByChannel(){

        return orderRepository.revenueByChannel()
                .stream()
                .map(r -> Map.of(
                        "channel", r[0],
                        "revenue", r[1]
                ))
                .toList();
    }

    public List<Map<String,Object>> revenueByDate(){

        return orderRepository.revenueByDate()
                .stream()
                .map(r -> Map.of(
                        "date", r[0],
                        "revenue", r[1]
                ))
                .toList();
    }

    public List<Map<String,Object>> ordersByStaff(){

        return orderRepository.ordersByStaff()
                .stream()
                .map(r -> Map.of(
                        "staff", r[0],
                        "orders", r[1]
                ))
                .toList();
    }
}