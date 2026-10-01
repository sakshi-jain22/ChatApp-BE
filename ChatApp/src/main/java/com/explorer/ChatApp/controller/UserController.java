package com.explorer.ChatApp.controller;

import com.explorer.ChatApp.dto.user.UpdateUserRequest;
import com.explorer.ChatApp.dto.user.UserResponse;
import com.explorer.ChatApp.entity.User;
import com.explorer.ChatApp.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);

        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        UserResponse response = UserResponse.from(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers()
                .stream()
                .map(UserResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(Authentication authentication, @RequestBody UpdateUserRequest request) {
        String email = authentication.getName();

        User currentUser = userService.getUserByEmail(email);

        User updatedUser = userService.updateUser(
                currentUser.getId(),
                request.getName(),
                request.getProfileImageUrl()
        );

        UserResponse response = UserResponse.from(updatedUser);
        return ResponseEntity.ok(response);
    }
}
