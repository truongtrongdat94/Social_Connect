package com.connect.social_connect.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqLoginDTO {
    @NotBlank(message = "Email không được để trống")
    private String username;

    @NotBlank(message = "Password không được để trống")
    private String password;
}
