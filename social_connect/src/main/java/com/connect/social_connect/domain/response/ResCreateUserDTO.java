package com.connect.social_connect.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCreateUserDTO {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private Instant createdAt;
}
