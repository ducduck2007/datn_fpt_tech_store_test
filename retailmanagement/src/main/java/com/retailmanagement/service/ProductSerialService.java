package com.retailmanagement.service;

import com.retailmanagement.entity.ProductSerial;
import com.retailmanagement.repository.ProductSerialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ProductSerialService {

    private final ProductSerialRepository serialRepository;

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    public String generateSerialNumber(Integer variantId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String variantPart = String.format("%03d", variantId);

        String serial;
        int attempts = 0;
        do {
            String random = generateRandom(4);
            serial = "SN-" + variantPart + "-" + date + "-" + random;
            attempts++;
            if (attempts > 100) throw new RuntimeException("Không thể gen serial unique sau 100 lần thử");
        } while (serialRepository.existsBySerialNumber(serial));

        return serial;
    }

    private String generateRandom(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    @Transactional
    public List<ProductSerial> generateAndSave(Integer variantId, int quantity) {
        List<ProductSerial> serials = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            ProductSerial serial = new ProductSerial();
            serial.setVariantId(variantId);
            serial.setSerialNumber(generateSerialNumber(variantId));
            serial.setStatus("IN_STOCK");
            serials.add(serial);
        }
        return serialRepository.saveAll(serials);
    }
}
