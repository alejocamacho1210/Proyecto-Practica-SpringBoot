package com.academy.security;

import com.academy.persistence.entity.Role;
import com.academy.persistence.entity.UserAccountEntity;
import com.academy.persistence.repository.UserAccountJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AccountSeeder implements CommandLineRunner {

    private final UserAccountJpaRepository accounts;
    private final PasswordEncoder encoder;

    public AccountSeeder(UserAccountJpaRepository accounts, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        createIfMissing("admin", "admin123", Role.ADMIN);
        createIfMissing("ana", "ana123", Role.USER);
    }

    private void createIfMissing(String username, String password, Role role) {
        if (accounts.findByUsername(username).isEmpty()) {
            accounts.save(new UserAccountEntity(username, encoder.encode(password), role));
        }
    }
}