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
@Table(name = "chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String type;

    @Column
    private String name;

    private Instant createdAt;
    private Instant updatedAt;

    // Automatically set before INSERT
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    // Automatically set before UPDATE
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
