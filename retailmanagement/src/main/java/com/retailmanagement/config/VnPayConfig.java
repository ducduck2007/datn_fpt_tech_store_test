package com.retailmanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VnPayConfig {
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_ReturnUrl = "http://localhost:5173/payment-result";
    public static String vnp_TmnCode = "2Z4PF208";
    public static String vnp_HashSecret = "ARF1ACDI5IMG6WYD4CM6T71KTX2QY1Q9";

    // Hàm tạo URL thanh toán chuẩn mực
    // Hàm tạo URL thanh toán có gắn Log dò lỗi
    public static String buildPaymentUrl(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);

            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Sử dụng hàm encode chuẩn RFC 3986
                String encodedValue = encodeValue(fieldValue);
                String encodedKey = encodeValue(fieldName);

                // Build HashData
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(encodedValue);

                // Build Query
                query.append(encodedKey);
                query.append('=');
                query.append(encodedValue);

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryStr = query.toString();
        String rawHashData = hashData.toString();

        // --- BẮT ĐẦU IN LOG KIỂM TRA ---
        System.out.println("\n========== [VNPAY DEBUG LOG] ==========");
        System.out.println("1. TmnCode đang dùng: " + vnp_TmnCode);
        System.out.println("2. HashSecret đang dùng: " + vnp_HashSecret);
        System.out.println("3. Chuỗi Query gửi đi: \n" + queryStr);
        System.out.println("4. Chuỗi RawData (để băm): \n" + rawHashData);

        // Băm chữ ký và ÉP IN HOA
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, rawHashData).toUpperCase();

        System.out.println("5. Mã băm SecureHash sinh ra (IN HOA): " + vnp_SecureHash);
        System.out.println("=======================================\n");

        return vnp_PayUrl + "?" + queryStr + "&vnp_SecureHash=" + vnp_SecureHash;
    }

    // Xử lý Encoding triệt để mọi ký tự đặc biệt
    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) throw new NullPointerException();
            final Mac hmac512 = Mac.getInstance("HmacSHA512");

            // ÉP CỨNG UTF-8 CHO KEY VÀ DATA
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                // Dùng %02X để xuất ra chữ in hoa ngay từ đầu cũng được, hoặc %02x rồi toUpperCase() ở trên
                sb.append(String.format("%02X", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            System.out.println("❌ LỖI TẠO MÃ BĂM HMACSHA512: " + ex.getMessage());
            return "";
        }
    }

    // Hàm này dùng riêng cho IPN và Return URL để kiểm tra chữ ký bảo mật
    public static String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                // Bắt buộc dùng encodeValue chuẩn RFC 3986 để so khớp chữ ký VNPAY trả về
                sb.append(encodeValue(fieldValue));
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return hmacSHA512(vnp_HashSecret, sb.toString());
    }
}