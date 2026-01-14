package com.connect.social_connect.domain;

import java.time.Instant;

import com.connect.social_connect.util.constant.AttachmentTypeEnum;

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
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "message_attachments")
@Getter
@Setter
public class MessageAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic fields
    @NotBlank(message = "fileUrl không được để trống")
    private String fileUrl;

    @NotBlank(message = "fileName không được để trống")
    private String fileName;

    private String mimeType;
    private Long fileSize;
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    private AttachmentTypeEnum type;

    private Integer displayOrder;

    // Audit fields
    private Instant createdAt;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    // Lifecycle
    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
    }
}
