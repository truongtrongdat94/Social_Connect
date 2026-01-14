package com.connect.social_connect.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResVerifyOtpDTO {
    private Boolean success;
    private String message;
    private ResLoginDTO loginData;
}
