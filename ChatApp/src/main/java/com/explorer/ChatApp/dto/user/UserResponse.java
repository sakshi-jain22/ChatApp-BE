package com.explorer.ChatApp.dto.user;

import com.explorer.ChatApp.entity.User;
import com.explorer.ChatApp.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String profileImageUrl;
    private UserStatus status;
    private Instant lastSeen;

    // Convert User entity -> UserResponse DTO
    public static UserResponse from(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getStatus(),
                user.getLastSeen()
        );
    }
}
