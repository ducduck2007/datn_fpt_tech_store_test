package com.retailmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "username không được để trống")
    @Size(min = 3, max = 50, message = "username 3-50 ký tự")
    private String username;

    @NotBlank(message = "email không được để trống")
    @Email(message = "email không hợp lệ")
    @Size(max = 100, message = "email tối đa 100 ký tự")
    private String email;

    @NotBlank(message = "password không được để trống")
    @Size(min = 6, max = 100, message = "password 6-100 ký tự")
    private String password;
}
