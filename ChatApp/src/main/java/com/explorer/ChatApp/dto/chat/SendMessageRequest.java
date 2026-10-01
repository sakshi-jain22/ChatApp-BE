package com.explorer.ChatApp.dto.chat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SendMessageRequest {
    private Long chatId;
    private String content;
}
