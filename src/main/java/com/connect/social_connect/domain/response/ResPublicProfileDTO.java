package com.connect.social_connect.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResPublicProfileDTO {
    private Long id;
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String coverUrl;
    private Instant createdAt;
}
