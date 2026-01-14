package com.connect.social_connect.domain.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateProfileDTO {
    private String displayName;

    @Size(max = 500, message = "Bio không được vượt quá 500 ký tự")
    private String bio;

    private String avatarUrl;

    private String coverUrl;
}
