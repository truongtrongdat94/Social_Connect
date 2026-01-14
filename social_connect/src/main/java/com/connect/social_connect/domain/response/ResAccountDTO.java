package com.connect.social_connect.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAccountDTO {
    private UserAccount user;

    @Getter
    @Setter
    public static class UserAccount {
        private Long id;
        private String email;
        private String username;
        private String displayName;
        private String bio;
        private String avatarUrl;
        private ResLoginDTO.RoleDTO role;
    }
}
