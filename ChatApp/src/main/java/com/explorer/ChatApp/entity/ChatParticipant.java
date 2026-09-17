package com.explorer.ChatApp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@Scope("prototype")
@Entity
@Table(name = "chat_participants", uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_user", columnNames = { "chat_id", "user_id" })
}, indexes = {
                @Index(name = "idx_participant_chat", columnList = "chat_id"),
                @Index(name = "idx_participant_user", columnList = "user_id")
})
public class ChatParticipant {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "chat_id", nullable = false, foreignKey = @ForeignKey(name = "fk_participant_chat"))
        private Chat chat;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_participant_user"))
        private User user;

        @Column(name = "joined_at", nullable = false, updatable = false)
        private Instant joinedAt;

        // @Column(name = "last_read_message_id")
        // private Long lastReadMessageId;

        @PrePersist
        protected void onCreate() {
                joinedAt = Instant.now();
        }
}