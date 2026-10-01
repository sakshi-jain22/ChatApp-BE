package com.explorer.ChatApp.service;

import com.explorer.ChatApp.entity.Chat;
import com.explorer.ChatApp.entity.Message;
import com.explorer.ChatApp.entity.User;
import com.explorer.ChatApp.enums.MessageStatus;
import com.explorer.ChatApp.enums.MessageType;
import com.explorer.ChatApp.exception.ResourceNotFoundException;
import com.explorer.ChatApp.repository.ChatParticipantRepository;
import com.explorer.ChatApp.repository.ChatRepository;
import com.explorer.ChatApp.repository.MessageRepository;
import com.explorer.ChatApp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
@Transactional
public class MessageService {

        private final MessageRepository messageRepository;
        private final ChatParticipantRepository chatParticipantRepository;
        private final ChatRepository chatRepository;
        private final UserRepository userRepository;

        /**
         * Send/save a new message.
         */
        @Transactional
        public Message sendMessage(
                        Long chatId,
                        String senderEmail,
                        String content
        ) {
                Chat chat = chatRepository.findById(chatId).orElseThrow(()->new ResourceNotFoundException("Chat", "id", chatId));
                User sender = userRepository.findByEmail(senderEmail).orElseThrow(()->new ResourceNotFoundException("User","email", senderEmail));

                // Make sure the sender belongs to this chat
                boolean isParticipant = chatParticipantRepository
                                .existsByChatIdAndUserId(
                                                chatId,
                                                sender.getId()
                                );

                if (!isParticipant) {
                        throw new IllegalStateException(
                                        "User is not a participant of this chat");
                }

                // Create message
                Message message = Message.builder()
                                .chat(chat)
                                .sender(sender)
                                .content(content)
                                .messageType(MessageType.TEXT) // Default value
                                .status(MessageStatus.SENT) // Default value
                                .build();

                // message.setChat(chat);
                // message.setSender(sender);
                // message.setContent(content);

                // // Default values
                // message.setStatus(MessageStatus.SENT);

                return messageRepository.save(message);
        }

        /**
         * Get paginated messages for a chat.
         */
        @Transactional(readOnly = true)
        public Page<Message> getMessages(
                        Long chatId,
                        Long userId,
                        Pageable pageable) {

                // Make sure user has access to this chat
                boolean isParticipant = chatParticipantRepository
                                .existsByChatIdAndUserId(
                                                chatId,
                                                userId);

                if (!isParticipant) {
                        throw new IllegalStateException(
                                        "User is not a participant of this chat");
                }

                return messageRepository
                                .findByChatIdOrderByCreatedAtDesc(
                                                chatId,
                                                pageable);
        }

        /**
         * Update message status.
         */
        public Message updateStatus(
                        Long messageId,
                        MessageStatus status) {

                Message message = messageRepository
                                .findById(messageId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Message not found"));

                message.setStatus(status);

                return messageRepository.save(message);
        }
}
