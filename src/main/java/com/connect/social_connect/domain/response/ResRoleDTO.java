package com.connect.social_connect.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResRoleDTO {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private List<ResPermissionDTO> permissions;
    private Instant createdAt;
    private Instant updatedAt;
}
