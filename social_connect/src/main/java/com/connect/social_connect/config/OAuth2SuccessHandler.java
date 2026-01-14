package com.connect.social_connect.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.connect.social_connect.domain.User;
import com.connect.social_connect.domain.response.ResLoginDTO;
import com.connect.social_connect.service.AuthService;
import com.connect.social_connect.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final AuthService authService;

    @Value("${app.oauth2.redirect-uri}")
    private String frontendRedirectUri;

    public OAuth2SuccessHandler(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        // 1. Get information from Google profile
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        // 2. Find or create user in database
        User user = userService.findOrCreateGoogleUser(email, name, picture);

        // 3. Create JWT tokens
        ResLoginDTO resLoginDTO = new ResLoginDTO();
        resLoginDTO.setUser(userService.convertToUserLogin(user));
        String accessToken = authService.createAccessToken(email, resLoginDTO);
        String refreshToken = authService.createRefreshToken(email, resLoginDTO);

        // 4. Save refresh token to database
        userService.updateUserToken(refreshToken, email);

        // 5. Set refresh token as HttpOnly cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(authService.getRefreshTokenExpiration())
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());

        // 6. Redirect to frontend with access token
        String redirectUrl = frontendRedirectUri + "?token=" + accessToken;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
