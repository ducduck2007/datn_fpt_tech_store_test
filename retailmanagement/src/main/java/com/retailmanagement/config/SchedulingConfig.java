package com.retailmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Spring sẽ tự động quét và chạy các @Scheduled methods
}

