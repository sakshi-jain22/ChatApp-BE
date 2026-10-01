package com.explorer.ChatApp.controller;

import com.explorer.ChatApp.dto.auth.LoginRequest;
import com.explorer.ChatApp.dto.auth.LoginResponse;
import com.explorer.ChatApp.dto.auth.RegisterRequest;
import com.explorer.ChatApp.dto.user.UserResponse;
import com.explorer.ChatApp.entity.User;
import com.explorer.ChatApp.service.AuthService;
import com.explorer.ChatApp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.getName(), request.getEmail(), request.getPassword());

        System.out.println("auth controller register: user - ");
        UserResponse response = UserResponse.from(user);
        System.out.println("response: " + response);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        String token = authService.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            @RequestHeader(
                    name = "Authorization",
                    required = false
            ) String authorizationHeader
    ) {
        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }

        User currentUser = userService.getUserByEmail(
                authentication.getName()
        );

        String token = authorizationHeader.substring(7);
        authService.logout(currentUser.getId(), token);

        return ResponseEntity.noContent().build();
    }
}
