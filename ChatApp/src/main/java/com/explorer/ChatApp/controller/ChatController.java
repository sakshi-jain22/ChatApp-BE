package com.explorer.ChatApp.controller;

import com.explorer.ChatApp.dto.chat.ChatResponse;
import com.explorer.ChatApp.dto.chat.CreateChatRequest;
import com.explorer.ChatApp.dto.chat.MessageResponse;
import com.explorer.ChatApp.entity.Chat;
import com.explorer.ChatApp.entity.Message;
import com.explorer.ChatApp.entity.User;
import com.explorer.ChatApp.service.ChatService;
import com.explorer.ChatApp.service.MessageService;
import com.explorer.ChatApp.service.UserService;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserService userService;

    public ChatController(ChatService chatService, MessageService messageService, UserService userService) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @PostMapping 
    public ResponseEntity<ChatResponse> createChat(Authentication authentication, @RequestBody CreateChatRequest request) {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);

        Chat chat = chatService.createChat(currentUser.getId(), request);

        ChatResponse response = ChatResponse.from(chat);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ChatResponse>> getUserChats(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);

        List<ChatResponse> response = chatService.getChatsForUser(currentUser.getId())
                .stream()
                .map(ChatResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatResponse> getChatById(
        Authentication authentication,
        @PathVariable Long chatId
    ) {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);

        Chat chat = chatService.getChatForUser(chatId, currentUser.getId());
        ChatResponse response = ChatResponse.from(chat);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Page<MessageResponse>> getChatMessages(
            Authentication authentication,
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email);

        // Prevent excessively large page sizes.
        int safeSize = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(
                page,
                safeSize,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<Message> messages = messageService.getMessages(
                chatId,
                currentUser.getId(),
                pageable
        );

        Page<MessageResponse> response = messages.map(
                MessageResponse::from
        );

        return ResponseEntity.ok(response);
    }
}
