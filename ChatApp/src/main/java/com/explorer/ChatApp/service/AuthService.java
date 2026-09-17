package com.explorer.ChatApp.service;

import com.explorer.ChatApp.entity.User;
import com.explorer.ChatApp.enums.UserStatus;
import com.explorer.ChatApp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User register(String name, String email, String password) {
        //Normalize email
        email = normalizeEmail(email);

        if(userRepository.existsByEmail(email)) {
            throw new IllegalStateException("User with this email already exists");
        }

        String passwordHash = passwordEncoder.encode(password);

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setStatus(UserStatus.OFFLINE);

        return userRepository.save(user);
    }

    public String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    @Transactional(readOnly = true)
    public String login(String email, String password) {
        email = normalizeEmail(email);

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(()-> new IllegalStateException("Invalid email or password"));

        boolean isPasswordMatched = passwordEncoder.matches(password, user.getPasswordHash());

        if (!isPasswordMatched) {
            throw new IllegalStateException("Invalid email or password");
        }

        return jwtService.generateToken(user);
    }
}
