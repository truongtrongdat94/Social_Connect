package com.connect.social_connect.domain;

import java.time.Instant;

import com.connect.social_connect.util.constant.NotificationTypeEnum;

import jakarta.persistence.Column;
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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic fields
    @Enumerated(EnumType.STRING)
    private NotificationTypeEnum type;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Long referenceId;
    private String referenceType;
    private Boolean isRead = false;

    // Audit fields
    private Instant createdAt;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    // Lifecycle
    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
    }
}
