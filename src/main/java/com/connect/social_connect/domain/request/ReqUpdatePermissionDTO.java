package com.connect.social_connect.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdatePermissionDTO {
    private String name;

    private String apiPath;

    private String method;

    private String module;
}
