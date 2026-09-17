package com.explorer.ChatApp.entity;

import com.explorer.ChatApp.enums.MessageStatus;
import com.explorer.ChatApp.enums.MessageType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Getter
@Setter
@Component
@Scope("prototype")
@Builder 
@Entity
@Table(name = "messages", indexes = {
                @Index(name = "idx_message_chat", columnList = "chat_id"),
                @Index(name = "idx_message_sender", columnList = "sender_id"),
                @Index(name = "idx_message_chat_created", columnList = "chat_id, created_at")
})
public class Message {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // Chat to which this message belongs
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "chat_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_chat"))
        private Chat chat;

        // User who sent the message
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "sender_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_sender"))
        private User sender;

        // Message content
        @Column(columnDefinition = "TEXT")
        private String content;

        // TEXT, IMAGE, VIDEO, FILE, etc.
        @Enumerated(EnumType.STRING)
        @Column(name = "message_type", nullable = false, length = 20)
        private MessageType messageType = MessageType.TEXT;

        // SENT, DELIVERED, READ
        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private MessageStatus status = MessageStatus.SENT;

        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        @PrePersist
        protected void onCreate() {
                Instant now = Instant.now();
                createdAt = now;
                updatedAt = now;
        }

        @PreUpdate
        protected void onUpdate() {
                updatedAt = Instant.now();
        }
}