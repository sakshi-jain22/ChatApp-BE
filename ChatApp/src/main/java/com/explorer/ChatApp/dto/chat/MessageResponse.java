package com.explorer.ChatApp.dto.chat;

import com.explorer.ChatApp.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long chatId;
    private Long senderId;
    private String senderName;
    private String content;
    private String messageType;
    private String status;
    private Instant createdAt;

    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getChat().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getContent(),
                message.getMessageType().name(),
                message.getStatus().name(),
                message.getCreatedAt()
        );
    }
}
