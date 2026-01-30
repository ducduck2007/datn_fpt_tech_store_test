package com.retailmanagement.service;

import org.springframework.stereotype.Service;

@Service
public class SettingService {

    // Tạm thời lưu in-memory
    // Có thể thay bằng DB / Redis / config sau
    private String defaultCurrency = "VND";

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public String setDefaultCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("currencyCode không được rỗng");
        }

        String c = currencyCode.trim().toUpperCase();
        if (c.length() != 3) {
            throw new IllegalArgumentException("currencyCode phải là ISO-4217 (3 ký tự)");
        }

        this.defaultCurrency = c;
        return this.defaultCurrency;
    }
}
