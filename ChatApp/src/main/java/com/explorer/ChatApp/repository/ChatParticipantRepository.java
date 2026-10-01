package com.explorer.ChatApp.repository;

import com.explorer.ChatApp.entity.ChatParticipant;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatParticipantRepository
                extends JpaRepository<ChatParticipant, Long> {

        Optional<ChatParticipant> findByChatIdAndUserId(Long chatId, Long userId);
        List<ChatParticipant> findByUserId(Long userId);
        List<ChatParticipant> findByChatId(Long chatId);
        void deleteByChatIdAndUserId(Long chatId, Long userId);

        boolean existsByChatIdAndUserId(
                        Long chatId,
                        Long userId);
}