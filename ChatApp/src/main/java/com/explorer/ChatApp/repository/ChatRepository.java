package com.explorer.ChatApp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.explorer.ChatApp.entity.Chat;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    boolean existsByIdAndIsGroupChatTrue(Long chatId);

    Page<Chat> findByIsGroupChatTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
