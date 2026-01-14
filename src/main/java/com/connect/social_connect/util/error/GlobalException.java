package com.connect.social_connect.util.error;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.connect.social_connect.domain.response.RestResponse;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object>> handleAllException(Exception ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setMessage(ex.getMessage());
        res.setError("Internal Server Error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    @ExceptionHandler(value = IdInvalidException.class)
    public ResponseEntity<RestResponse<Object>> handleIdInvalidException(IdInvalidException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(ex.getMessage());
        res.setError("Exception occurs...");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    public ResponseEntity<RestResponse<Object>> handleBadCredentialsException(BadCredentialsException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage("Username/password không hợp lệ");
        res.setError("Authentication Error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(value = UsernameNotFoundException.class)
    public ResponseEntity<RestResponse<Object>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage("Username/password không hợp lệ");
        res.setError("Authentication Error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(value = JwtException.class)
    public ResponseEntity<RestResponse<Object>> handleJwtException(JwtException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setMessage("Token không hợp lệ");
        res.setError("Token Error");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> validationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getBody().getDetail());

        List<String> errors = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        res.setMessage(errors.size() > 1 ? errors : errors.get(0));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(PermissionException.class)
    public ResponseEntity<RestResponse<Object>> handlePermissionException(Exception ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setError("Forbidden");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<RestResponse<Object>> handleStorageException(Exception ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("Storage Error");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RestResponse<Object>> handleNotFoundException(Exception ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setMessage(ex.getMessage());
        res.setError("404 Not Found. URL may not exist...");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    // OTP Exception Handlers - Requirements: 2.2, 2.3, 2.5, 3.3

    @ExceptionHandler(OtpInvalidException.class)
    public ResponseEntity<RestResponse<Object>> handleOtpInvalidException(OtpInvalidException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("OTP_INVALID");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<RestResponse<Object>> handleOtpExpiredException(OtpExpiredException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError("OTP_EXPIRED");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(OtpLockedException.class)
    public ResponseEntity<RestResponse<Object>> handleOtpLockedException(OtpLockedException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
        res.setError("OTP_LOCKED");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(res);
    }

    @ExceptionHandler(OtpResendLimitException.class)
    public ResponseEntity<RestResponse<Object>> handleOtpResendLimitException(OtpResendLimitException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
        res.setError("OTP_RESEND_LIMIT");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(res);
    }

    // OAuth2 Exception Handlers - Requirements: 4.6, 6.4

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<RestResponse<Object>> handleOAuth2AuthenticationException(OAuth2AuthenticationException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setError("OAUTH2_FAILED");
        res.setMessage("Đăng nhập Google thất bại: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }
}
