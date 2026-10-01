package com.explorer.ChatApp.service;

import com.explorer.ChatApp.dto.chat.CreateChatRequest;
import com.explorer.ChatApp.entity.Chat;
import com.explorer.ChatApp.entity.ChatParticipant;
import com.explorer.ChatApp.entity.User;
import com.explorer.ChatApp.enums.ChatType;
import com.explorer.ChatApp.exception.ResourceNotFoundException;
import com.explorer.ChatApp.exception.UnauthorizedException;
import com.explorer.ChatApp.repository.ChatParticipantRepository;
import com.explorer.ChatApp.repository.ChatRepository;
import com.explorer.ChatApp.repository.UserRepository;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Transactional
public class ChatService {
        private final ChatRepository chatRepository;
        private final ChatParticipantRepository chatParticipantRepository;
        private final UserRepository userRepository;

        private void validateCreateChatRequest(CreateChatRequest request) {
                if (request == null) {
                        throw new IllegalArgumentException("Chat request cannot be null.");
                }

                if (request.getType() == null) {
                        throw new IllegalArgumentException("Request type is required.");
                }

                if (request.getType() == ChatType.GROUP) {
                        if (request.getName() == null || (request.getName().trim().isEmpty())) {
                                throw new IllegalArgumentException("Group Chat name is required.");
                        }
                }

                if (request.getParticipantIds() == null ||
                                request.getParticipantIds().isEmpty()) {

                        throw new IllegalArgumentException(
                                        "At least one participant is required");
                }
        }

        @Transactional
        public Chat findExistingPrivateChat(
                        Long currentUserId,
                        Long otherUserId) {
                List<ChatParticipant> currentUserParticipants = chatParticipantRepository
                                .findByUserId(currentUserId);
                for (ChatParticipant participant : currentUserParticipants) {
                        Chat chat = participant.getChat();

                        if (chat.getType() != ChatType.PRIVATE) {
                                continue;
                        }
                        boolean otherUserIsParticipant = chatParticipantRepository
                                        .existsByChatIdAndUserId(
                                                        chat.getId(),
                                                        otherUserId);

                        if (otherUserIsParticipant) {
                                return chat;
                        }
                }

                return null;
        }

        public Chat createChat(
                        Long currentUserId,
                        CreateChatRequest request) {
                validateCreateChatRequest(request);

                getUserById(currentUserId);

                Chat chat = new Chat();
                chat.setType(request.getType());
                chat.setName(request.getName());

                Instant now = Instant.now();
                chat.setCreatedAt(now);
                chat.setUpdatedAt(now);

                Chat savedChat = chatRepository.save(chat);

                Set<Long> participantIds = new HashSet<>();
                participantIds.add(currentUserId);

                if (request.getParticipantIds() != null) {
                        participantIds.addAll(request.getParticipantIds());
                }

                for (Long participantId : participantIds) {
                        User participant = getUserById(participantId);
                        ChatParticipant chatParticipant = new ChatParticipant();
                        chatParticipant.setChat(savedChat);
                        chatParticipant.setUser(participant);
                        chatParticipant.setJoinedAt(Instant.now());

                        chatParticipantRepository.save(chatParticipant);
                }
                return savedChat;
        }

        public Chat createPrivateChat(
                        Long currentUserId,
                        Long otherUserId) {

                if (currentUserId.equals(otherUserId)) {
                        throw new IllegalArgumentException(
                                        "You cannot create a private chat with yourself");
                }

                Chat existingChat = findExistingPrivateChat(
                                currentUserId,
                                otherUserId);

                if (existingChat != null) {
                        return existingChat;
                }

                CreateChatRequest request = new CreateChatRequest();

                request.setType(ChatType.PRIVATE);
                request.setName(null);
                request.setParticipantIds(
                                List.of(otherUserId));

                return createChat(currentUserId, request);
        }

        @Transactional
        public List<Chat> getChatsForUser(
                        Long userId) {
                getUserById(userId);

                List<ChatParticipant> participants = chatParticipantRepository.findByUserId(userId);
                List<Chat> chats = new ArrayList<>();

                for (ChatParticipant chatParticipant : participants) {
                        chats.add(chatParticipant.getChat());
                }

                return chats;
        }

        @Transactional
        public Chat getChatForUser(
                        Long chatId,
                        Long userId) {
                Chat chat = getChatById(chatId);
                getUserById(userId);

                boolean isParticipant = isUserParticipant(chatId, userId);

                if (!isParticipant) {
                        throw new UnauthorizedException("You are not authorized to access this chat");
                }

                return chat;
        }

        @Transactional
        public boolean isUserParticipant(Long chatId, Long userId) {
                return chatParticipantRepository.existsByChatIdAndUserId(chatId, userId);
        }

        public ChatParticipant addParticipant(Long chatId, Long userId) {
                Chat chat = getChatById(chatId);
                User user = getUserById(userId);

                boolean isAlreadyAParticipant = chatParticipantRepository.existsByChatIdAndUserId(chatId, userId);

                if (isAlreadyAParticipant) {
                        throw new IllegalStateException("User is already a participant in this chat.");
                }

                ChatParticipant chatParticipant = new ChatParticipant();
                chatParticipant.setChat(chat);
                chatParticipant.setUser(user);
                chatParticipant.setJoinedAt(Instant.now());

                return chatParticipantRepository.save(chatParticipant);
        }

        public void removeParticipant(Long chatId, Long userId) {
                getChatById(chatId);
                getUserById(userId);

                ChatParticipant participant = chatParticipantRepository
                                .findByChatIdAndUserId(chatId, userId)
                                .orElseThrow(() -> new IllegalStateException("User is not a participant of the chat."));

                chatParticipantRepository.delete(participant);
        }

        public void deleteChat(Long chatId, Long userId) {
                getChatForUser(chatId, userId);

                chatRepository.deleteById(chatId);
        }

        @Transactional
        public User getUserById(Long userId) {
                return userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "User",
                                        "id",
                                        userId
                                ));
        }

        private Chat getChatById(Long chatId) {
                return chatRepository.findById(chatId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Chat",
                                        "id",
                                        chatId
                                ));
        }
}
