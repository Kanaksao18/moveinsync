package com.moveinsync.mdm.controller;

import com.moveinsync.mdm.dto.AuthResponse;
import com.moveinsync.mdm.dto.LoginRequest;
import com.moveinsync.mdm.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return new AuthResponse(service.login(request));
    }
}