package com.connect.social_connect.config;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.connect.social_connect.domain.Permission;
import com.connect.social_connect.domain.Role;
import com.connect.social_connect.domain.User;
import com.connect.social_connect.service.UserService;
import com.connect.social_connect.util.SecurityUtil;
import com.connect.social_connect.util.error.PermissionException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PermissionInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public PermissionInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String httpMethod = request.getMethod();

        String email = SecurityUtil.getCurrentUserLogin().orElse("");

        if (email == null || email.isEmpty()) {
            throw new PermissionException("Bạn cần đăng nhập để truy cập endpoint này.");
        }

        User user = this.userService.handleGetUserByUsername(email);
        if (user == null) {
            throw new PermissionException("Không tìm thấy thông tin người dùng.");
        }

        Role role = user.getRole();
        if (role == null) {
            throw new PermissionException("Bạn không có quyền truy cập endpoint này.");
        }

        List<Permission> permissions = role.getPermissions();
        if (permissions == null || permissions.isEmpty()) {
            throw new PermissionException("Bạn không có quyền truy cập endpoint này.");
        }

        boolean isAllowed = permissions.stream()
                .anyMatch(p -> p.getApiPath().equals(path) && p.getMethod().equals(httpMethod));

        if (!isAllowed) {
            throw new PermissionException("Bạn không có quyền truy cập endpoint này.");
        }

        return true;
    }
}
