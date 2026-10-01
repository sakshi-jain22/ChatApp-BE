package com.explorer.ChatApp.controller;

import com.explorer.ChatApp.dto.chat.MessageResponse;
import com.explorer.ChatApp.dto.chat.SendMessageRequest;
import com.explorer.ChatApp.entity.Message;
import com.explorer.ChatApp.security.CustomWebSocketPrincipal;
import com.explorer.ChatApp.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.security.Principal;

@Controller
public class ChatWebSocketController {
    private final MessageService messageService;

    public ChatWebSocketController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/chat.send")
//    @SendTo("/topic/chat/{chatId}")
    public MessageResponse sendMessage(
            SendMessageRequest request,
            Principal principal
    ) {
        if (principal == null) {
            throw new IllegalStateException(
                    "User is not authenticated"
            );
        }

        if (request.getChatId() == null) {
            throw new IllegalArgumentException("Chat Id is required");
        }

        if (!StringUtils.hasText(request.getContent())) {
            throw new IllegalArgumentException("Message content cannot be empty.");
        }

        CustomWebSocketPrincipal customPrincipal =
                (CustomWebSocketPrincipal) principal;

        String senderEmail = customPrincipal.getEmail();

        Message message = messageService.sendMessage(
                request.getChatId(),
                senderEmail,
                request.getContent()
        );

        return MessageResponse.from(message);
    }
}
