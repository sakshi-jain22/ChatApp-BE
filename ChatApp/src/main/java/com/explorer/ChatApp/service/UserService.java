package com.explorer.ChatApp.service;

import com.explorer.ChatApp.entity.User;
import com.explorer.ChatApp.enums.UserStatus;
import com.explorer.ChatApp.repository.UserRepository;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public User createUser(String name, String email, String passwordHash) {
        if(userRepository.existsByEmail(email)) {
            throw new IllegalStateException("User with this email already exists.");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setLastSeen(Instant.now());
        user.setStatus(UserStatus.OFFLINE);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long userId, String name, String profileImageUrl) {
        User user = getUserById(userId);

        if (name != null && !name.isBlank()) user.setName(name);

        if(profileImageUrl != null) user.setProfileImageUrl(profileImageUrl);

        return userRepository.save(user);
    }

    public User updateStatus(Long userId, UserStatus status) {
        User user = getUserById(userId);

        user.setStatus(status);

        if(status == UserStatus.OFFLINE) user.setLastSeen(Instant.now());

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if(!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        userRepository.deleteById(userId);
    }
}
