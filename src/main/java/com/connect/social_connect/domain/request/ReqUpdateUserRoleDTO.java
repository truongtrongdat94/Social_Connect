package com.connect.social_connect.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateUserRoleDTO {
    @NotNull(message = "Role ID không được để trống")
    private Long roleId;
}
