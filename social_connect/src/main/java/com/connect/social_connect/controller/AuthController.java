package com.connect.social_connect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.connect.social_connect.domain.User;
import com.connect.social_connect.domain.request.ReqLoginDTO;
import com.connect.social_connect.domain.request.ReqRegisterDTO;
import com.connect.social_connect.domain.request.ReqResendOtpDTO;
import com.connect.social_connect.domain.request.ReqVerifyOtpDTO;
import com.connect.social_connect.domain.response.ResAccountDTO;
import com.connect.social_connect.domain.response.ResCreateUserDTO;
import com.connect.social_connect.domain.response.ResLoginDTO;
import com.connect.social_connect.domain.response.ResResendOtpDTO;
import com.connect.social_connect.domain.response.ResVerifyOtpDTO;
import com.connect.social_connect.service.AuthService;
import com.connect.social_connect.service.EmailService;
import com.connect.social_connect.service.OtpService;
import com.connect.social_connect.service.UserService;
import com.connect.social_connect.util.SecurityUtil;
import com.connect.social_connect.util.annotation.ApiMessage;
import com.connect.social_connect.util.constant.AuthProviderEnum;
import com.connect.social_connect.util.error.IdInvalidException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public AuthController(
            UserService userService,
            AuthService authService,
            OtpService otpService,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.userService = userService;
        this.authService = authService;
        this.otpService = otpService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @PostMapping("/register")
    @ApiMessage("Đăng ký tài khoản thành công")
    public ResponseEntity<ResCreateUserDTO> register(@Valid @RequestBody ReqRegisterDTO registerDTO)
            throws IdInvalidException {
        // Check if email already exists
        if (userService.isEmailExist(registerDTO.getEmail())) {
            throw new IdInvalidException("Email đã tồn tại");
        }

        // Create new user with isEmailVerified = false and authProvider = LOCAL
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));
        user.setDisplayName(registerDTO.getDisplayName());
        user.setIsEmailVerified(false);
        user.setAuthProvider(AuthProviderEnum.LOCAL);

        // Save user (default USER role is assigned in UserService)
        User savedUser = userService.handleCreateUser(user);

        // Generate OTP and send email
        String otpCode = otpService.generateOtp(savedUser.getEmail());
        emailService.sendOtpEmail(savedUser.getEmail(), otpCode);

        // Convert to response DTO
        ResCreateUserDTO response = userService.convertToResCreateUserDTO(savedUser);
        response.setMessage("Mã OTP đã được gửi đến email của bạn. Vui lòng xác thực để hoàn tất đăng ký.");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify-otp")
    @ApiMessage("Xác thực OTP thành công")
    public ResponseEntity<ResVerifyOtpDTO> verifyOtp(@Valid @RequestBody ReqVerifyOtpDTO verifyOtpDTO)
            throws IdInvalidException {
        String email = verifyOtpDTO.getEmail();
        String otpCode = verifyOtpDTO.getOtpCode();

        // Check if user exists
        User user = userService.handleGetUserByUsername(email);
        if (user == null) {
            throw new IdInvalidException("Email không tồn tại trong hệ thống");
        }

        // Check if already verified
        if (user.getIsEmailVerified()) {
            throw new IdInvalidException("Email đã được xác thực trước đó");
        }

        // Check if OTP is locked
        if (otpService.isOtpLocked(email)) {
            throw new IdInvalidException("Tài khoản bị khóa do nhập sai OTP quá nhiều lần. Vui lòng thử lại sau.");
        }

        // Verify OTP
        boolean isValid = otpService.verifyOtp(email, otpCode);

        ResVerifyOtpDTO response = new ResVerifyOtpDTO();

        if (!isValid) {
            // Check if it's expired or invalid
            response.setSuccess(false);
            response.setMessage("Mã OTP không hợp lệ hoặc đã hết hạn");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Activate user (set isEmailVerified = true)
        User activatedUser = userService.activateUser(email);

        // Send welcome email
        emailService.sendWelcomeEmail(email, activatedUser.getDisplayName());

        response.setSuccess(true);
        response.setMessage("Xác thực email thành công. Tài khoản của bạn đã được kích hoạt.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    @ApiMessage("Gửi lại OTP thành công")
    public ResponseEntity<ResResendOtpDTO> resendOtp(@Valid @RequestBody ReqResendOtpDTO resendOtpDTO)
            throws IdInvalidException {
        String email = resendOtpDTO.getEmail();

        // Check if user exists
        User user = userService.handleGetUserByUsername(email);
        if (user == null) {
            throw new IdInvalidException("Email không tồn tại trong hệ thống");
        }

        // Check if already verified
        if (user.getIsEmailVerified()) {
            throw new IdInvalidException("Email đã được xác thực trước đó");
        }

        // Check remaining resend attempts
        int remainingAttempts = otpService.getRemainingResendAttempts(email);
        if (remainingAttempts <= 0) {
            throw new IdInvalidException("Đã vượt quá số lần gửi lại OTP. Vui lòng đợi 30 phút trước khi thử lại.");
        }

        // Resend OTP
        try {
            String newOtpCode = otpService.resendOtp(email);
            emailService.sendOtpEmail(email, newOtpCode);
        } catch (IllegalStateException e) {
            throw new IdInvalidException(e.getMessage());
        }

        ResResendOtpDTO response = new ResResendOtpDTO();
        response.setSuccess(true);
        response.setMessage("Mã OTP mới đã được gửi đến email của bạn.");
        response.setRemainingAttempts(otpService.getRemainingResendAttempts(email));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @ApiMessage("Đăng nhập thành công")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO,
            HttpServletResponse response) {
        // Authenticate with AuthenticationManager
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // Set SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user from database
        User currentUser = userService.handleGetUserByUsername(loginDTO.getUsername());

        // Build response DTO
        ResLoginDTO resLoginDTO = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = userService.convertToUserLogin(currentUser);
        resLoginDTO.setUser(userLogin);

        // Create access token
        String accessToken = authService.createAccessToken(loginDTO.getUsername(), resLoginDTO);
        resLoginDTO.setAccessToken(accessToken);

        // Create refresh token
        String refreshToken = authService.createRefreshToken(loginDTO.getUsername(), resLoginDTO);

        // Store refresh token in database
        userService.updateUserToken(refreshToken, loginDTO.getUsername());

        // Set refresh token as HttpOnly cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(authService.getRefreshTokenExpiration())
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(resLoginDTO);
    }

    @GetMapping("/refresh")
    @ApiMessage("Làm mới token thành công")
    public ResponseEntity<ResLoginDTO> refreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken,
            HttpServletResponse response) throws IdInvalidException {
        // Check if refresh token is provided
        if (refreshToken.isEmpty()) {
            throw new IdInvalidException("Refresh token không hợp lệ");
        }

        // Validate refresh token (will throw exception if invalid/expired)
        try {
            authService.checkValidRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new IdInvalidException("Refresh token không hợp lệ");
        }

        // Check if refresh token exists in database
        User currentUser = userService.getUserByRefreshToken(refreshToken);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh token không hợp lệ");
        }

        // Build response DTO
        ResLoginDTO resLoginDTO = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = userService.convertToUserLogin(currentUser);
        resLoginDTO.setUser(userLogin);

        // Create new access token
        String newAccessToken = authService.createAccessToken(currentUser.getEmail(), resLoginDTO);
        resLoginDTO.setAccessToken(newAccessToken);

        // Create new refresh token (token rotation)
        String newRefreshToken = authService.createRefreshToken(currentUser.getEmail(), resLoginDTO);

        // Update stored refresh token in database
        userService.updateUserToken(newRefreshToken, currentUser.getEmail());

        // Update refresh token cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(authService.getRefreshTokenExpiration())
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(resLoginDTO);
    }

    @PostMapping("/logout")
    @ApiMessage("Đăng xuất thành công")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", defaultValue = "") String refreshToken,
            HttpServletResponse response) throws IdInvalidException {

        // Get user from refresh token (if available)
        String email = null;

        // Try to get email from SecurityContext first (if user has valid access token)
        email = SecurityUtil.getCurrentUserLogin().orElse(null);

        // If no access token, try to get email from refresh token
        if (email == null && !refreshToken.isEmpty()) {
            User user = userService.getUserByRefreshToken(refreshToken);
            if (user != null) {
                email = user.getEmail();
            }
        }

        // Clear refresh token from database if we found the user
        if (email != null) {
            userService.updateUserToken(null, email);
        }

        // Clear refresh token cookie (maxAge=0)
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(null);
    }

    @GetMapping("/account")
    @ApiMessage("Lấy thông tin tài khoản thành công")
    public ResponseEntity<ResAccountDTO> getAccount() throws IdInvalidException {
        // Get current user from SecurityContext
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy người dùng"));

        // Get user from database
        User currentUser = userService.handleGetUserByUsername(email);
        if (currentUser == null) {
            throw new IdInvalidException("Không tìm thấy người dùng");
        }

        // Convert to response DTO (excludes password and refresh token)
        ResAccountDTO response = userService.convertToResAccountDTO(currentUser);

        return ResponseEntity.ok(response);
    }
}
