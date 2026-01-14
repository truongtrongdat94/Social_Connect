package com.connect.social_connect.domain.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateRoleDTO {
    private String name;

    private String description;

    private Boolean active;

    private List<Long> permissionIds;
}
