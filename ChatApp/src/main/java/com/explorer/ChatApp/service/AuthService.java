package com.explorer.ChatApp.service;

import com.explorer.ChatApp.entity.User;
import com.explorer.ChatApp.enums.UserStatus;
import com.explorer.ChatApp.exception.ResourceNotFoundException;
import com.explorer.ChatApp.repository.UserRepository;
import com.explorer.ChatApp.security.JwtService;
import com.explorer.ChatApp.security.TokenBlacklistService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(
            UserRepository userRepository,
            @Lazy PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public User register(String name, String email, String password) {
        //Normalize email
        email = normalizeEmail(email);

        System.out.println("23: register: "+name + " " + email +" "+password);
        if(userRepository.existsByEmail(email)) {
            throw new IllegalStateException("User with this email already exists");
        }

        String passwordHash = passwordEncoder.encode(password);

        System.out.println("30: "+password);
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setStatus(UserStatus.OFFLINE);

        System.out.println("37: user: "+ user);
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

        user.setStatus(UserStatus.ONLINE);
        user.setLastSeen(null);
        System.out.println("User: " + user.toString());
        userRepository.save(user);

        return jwtService.generateToken(user);
    }

    public void logout(Long userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User",
                                "id",
                                userId
                        ));
        String tokenId = jwtService.extractTokenId(token);
        Instant expiration = jwtService.extractExpiration(token);
        tokenBlacklistService.revokeToken(
                tokenId,
                expiration
        );

        user.setStatus(UserStatus.OFFLINE);
        user.setLastSeen(Instant.now());
        userRepository.save(user);
    }
}
