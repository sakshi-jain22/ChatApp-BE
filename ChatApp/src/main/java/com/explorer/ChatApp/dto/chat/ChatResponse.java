package com.explorer.ChatApp.dto.chat;

import com.explorer.ChatApp.entity.Chat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ChatResponse {
    private Long id;
    private String type;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;

    public static ChatResponse from(Chat chat) {

        return new ChatResponse(
                chat.getId(),
                chat.getType().name(),
                chat.getName(),
                chat.getCreatedAt(),
                chat.getUpdatedAt()
        );
    }
}
