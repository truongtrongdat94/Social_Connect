package com.connect.social_connect.domain;

import java.time.Instant;

import com.connect.social_connect.util.constant.ChatRoleEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "chat_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "chat_id", "user_id" })
})
@Getter
@Setter
public class ChatMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic fields
    @Enumerated(EnumType.STRING)
    private ChatRoleEnum role = ChatRoleEnum.MEMBER;

    // Audit fields
    private Instant joinedAt;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Lifecycle
    @PrePersist
    public void handleBeforeCreate() {
        this.joinedAt = Instant.now();
    }
}
