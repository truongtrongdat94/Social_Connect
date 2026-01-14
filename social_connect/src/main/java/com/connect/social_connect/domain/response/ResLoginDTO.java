package com.connect.social_connect.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResLoginDTO {
    private String accessToken;
    private UserLogin user;

    @Getter
    @Setter
    public static class UserLogin {
        private Long id;
        private String email;
        private String username;
        private String displayName;
        private RoleDTO role;
    }

    @Getter
    @Setter
    public static class RoleDTO {
        private Long id;
        private String name;
    }
}
