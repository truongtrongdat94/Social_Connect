package com.connect.social_connect.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.connect.social_connect.domain.Role;
import com.connect.social_connect.repository.RoleRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DatabaseSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting database seeding...");
        seedRoles();
        log.info("Database seeding completed.");
    }

    private void seedRoles() {
        // Seed USER role
        if (roleRepository.findByName("USER") == null) {
            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("Default user role");
            userRole.setActive(true);
            roleRepository.save(userRole);
            log.info("Created default USER role");
        }

        // Seed ADMIN role
        if (roleRepository.findByName("ADMIN") == null) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Administrator role");
            adminRole.setActive(true);
            roleRepository.save(adminRole);
            log.info("Created default ADMIN role");
        }
    }
}
