package com.connect.social_connect.domain;

import com.connect.social_connect.util.constant.MediaTypeEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "post_media", indexes = {
        @Index(name = "idx_post_media_post", columnList = "post_id")
})
@Getter
@Setter
public class PostMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic fields
    @NotBlank(message = "mediaUrl không được để trống")
    private String mediaUrl;

    @NotNull(message = "mediaType không được để trống")
    @Enumerated(EnumType.STRING)
    private MediaTypeEnum mediaType;

    private Integer displayOrder = 0;

    // Soft delete
    private Boolean isDeleted = false;
    private java.time.Instant deletedAt;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}
