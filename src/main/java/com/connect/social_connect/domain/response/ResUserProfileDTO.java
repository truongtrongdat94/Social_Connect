package com.connect.social_connect.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResUserProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String coverUrl;
    private String authProvider;
    private Boolean isEmailVerified;
    private RoleDTO role;
    private Instant createdAt;

    @Getter
    @Setter
    public static class RoleDTO {
        private Long id;
        private String name;
    }
}
