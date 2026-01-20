package com.retailmanagement.controller;

import com.retailmanagement.dto.response.UserLoginResponse;
import com.retailmanagement.service.UserLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
