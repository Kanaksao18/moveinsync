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

        if (adminRepo.findByUsername("admin").isEmpty()) {

            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");

            adminRepo.save(admin);

            System.out.println("✅ Default admin user created (admin / admin123)");
        } else {
            System.out.println("ℹ️ Admin user already exists");
        }
    }
}