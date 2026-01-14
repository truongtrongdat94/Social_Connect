package com.connect.social_connect.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = "Đăng nhập Google thất bại";

        if (exception.getMessage() != null) {
            errorMessage = exception.getMessage();
        }

        // URL encode the error message
        String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

        // Redirect to frontend with error parameter
        String redirectUrl = frontendRedirectUri + "?error=" + encodedError;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
