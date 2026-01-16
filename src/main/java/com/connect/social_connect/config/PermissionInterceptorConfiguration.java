package com.connect.social_connect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.connect.social_connect.service.UserService;

@Configuration
public class PermissionInterceptorConfiguration implements WebMvcConfigurer {

    private final UserService userService;

    public PermissionInterceptorConfiguration(UserService userService) {
        this.userService = userService;
    }

    @Bean
    PermissionInterceptor getPermissionInterceptor() {
        return new PermissionInterceptor(userService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Whitelist endpoints that don't require permission checking
        String[] whiteList = {
                "/",
                "/api/v1/auth/**",
                "/oauth2/**",
                "/login/oauth2/**",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                // User profile endpoints - public profile viewing
                "/api/v1/users/{id}",
                // Current user profile endpoints - authenticated but no permission check needed
                "/api/v1/users/me"
        };

        registry.addInterceptor(getPermissionInterceptor())
                .excludePathPatterns(whiteList);
    }
}
