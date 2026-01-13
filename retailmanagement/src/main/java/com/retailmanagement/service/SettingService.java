package com.retailmanagement.service;

import com.retailmanagement.entity.SystemSetting;
import com.retailmanagement.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SettingService {
    public static final String DEFAULT_CURRENCY_KEY = "DEFAULT_CURRENCY";

    private final SystemSettingRepository repo;

    public SettingService(SystemSettingRepository repo) {
        this.repo = repo;
    }

    public String getDefaultCurrency() {
        return repo.findById(DEFAULT_CURRENCY_KEY)
                .map(SystemSetting::getValue)
                .orElse("VND");
    }

    @Transactional
    public String setDefaultCurrency(String code) {
        if (code == null || code.trim().length() != 3) {
            throw new IllegalArgumentException("currencyCode phải là mã 3 ký tự (VD: VND, USD)");
        }
        String normalized = code.trim().toUpperCase();

        SystemSetting s = repo.findById(DEFAULT_CURRENCY_KEY).orElseGet(SystemSetting::new);
        s.setKey(DEFAULT_CURRENCY_KEY);
        s.setValue(normalized);
        s.setUpdatedAt(LocalDateTime.now());
        repo.save(s);

        return normalized;
    }
}
