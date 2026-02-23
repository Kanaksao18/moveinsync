package com.moveinsync.mdm.service;

import com.moveinsync.mdm.dto.AuthResponse;
import com.moveinsync.mdm.dto.LoginRequest;
import com.moveinsync.mdm.entity.AdminUser;
import com.moveinsync.mdm.exception.UnauthorizedException;
import com.moveinsync.mdm.repository.AdminUserRepository;
import com.moveinsync.mdm.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest request) {

        AdminUser user = repo.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getRole());
    }
}
