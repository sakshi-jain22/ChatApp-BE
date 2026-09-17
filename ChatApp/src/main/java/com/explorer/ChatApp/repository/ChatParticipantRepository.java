package com.explorer.ChatApp.repository;

import com.explorer.ChatApp.entity.ChatParticipant;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatParticipantRepository
                extends JpaRepository<ChatParticipant, Long> {

        List<ChatParticipant> findByChatId(Long chatId);

        boolean existsByChatIdAndUserId(
                        Long chatId,
                        Long userId);
}