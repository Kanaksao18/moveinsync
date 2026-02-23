package com.moveinsync.mdm.config;

import com.moveinsync.mdm.entity.AdminUser;
import com.moveinsync.mdm.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AdminUserRepository adminRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserIfMissing("admin", "admin123", "ADMIN");
        createUserIfMissing("viewer", "viewer123", "VIEWER");
        createUserIfMissing("producthead", "product123", "PRODUCT_HEAD");
    }

    private void createUserIfMissing(String username, String password, String role) {
        if (adminRepo.findByUsername(username).isPresent()) {
            return;
        }

        AdminUser user = new AdminUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        adminRepo.save(user);
    }
}
