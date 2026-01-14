package com.connect.social_connect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending emails using Spring Mail and Gmail SMTP.
 * Handles sending OTP verification emails and welcome emails.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.application.name:Social Connect}")
    private String applicationName;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send OTP verification email to the user.
     * The email contains the OTP code and clear instructions.
     */
    public void sendOtpEmail(String to, String otpCode) {
        String subject = "Xác thực email - " + applicationName;
        String htmlContent = buildOtpEmailContent(otpCode);

        sendHtmlEmail(to, subject, htmlContent);
        log.info("OTP email sent successfully to: {}", maskEmail(to));
    }

    /**
     * Send welcome email after successful email verification.
     */
    public void sendWelcomeEmail(String to, String displayName) {
        String subject = "Chào mừng đến với " + applicationName;
        String htmlContent = buildWelcomeEmailContent(displayName);

        sendHtmlEmail(to, subject, htmlContent);
        log.info("Welcome email sent successfully to: {}", maskEmail(to));
    }


    /**
     * Send HTML email using JavaMailSender.
     * Handles connection failures gracefully and logs errors.
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to {}: {}", maskEmail(to), e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.", e);
        }
    }

    /**
     * Build HTML content for OTP verification email.
     */
    private String buildOtpEmailContent(String otpCode) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 28px;">%s</h1>
                </div>
                <div style="background-color: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; border: 1px solid #ddd; border-top: none;">
                    <h2 style="color: #333; margin-top: 0;">Xác thực email của bạn</h2>
                    <p>Xin chào,</p>
                    <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>%s</strong>. Để hoàn tất quá trình đăng ký, vui lòng sử dụng mã OTP dưới đây:</p>
                    <div style="background-color: #667eea; color: white; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 8px; border-radius: 8px; margin: 25px 0;">
                        %s
                    </div>
                    <p style="color: #666; font-size: 14px;">
                        <strong>Lưu ý:</strong>
                    </p>
                    <ul style="color: #666; font-size: 14px;">
                        <li>Mã OTP này có hiệu lực trong <strong>%d phút</strong></li>
                        <li>Không chia sẻ mã này với bất kỳ ai</li>
                        <li>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này</li>
                    </ul>
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 25px 0;">
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        Email này được gửi tự động từ %s. Vui lòng không trả lời email này.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(applicationName, applicationName, otpCode, otpExpirationMinutes, applicationName);
    }

    /**
     * Build HTML content for welcome email after successful verification.
     */
    private String buildWelcomeEmailContent(String displayName) {
        return """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 28px;">%s</h1>
                </div>
                <div style="background-color: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; border: 1px solid #ddd; border-top: none;">
                    <h2 style="color: #333; margin-top: 0;">Chào mừng, %s! 🎉</h2>
                    <p>Xin chúc mừng! Tài khoản của bạn đã được xác thực thành công.</p>
                    <p>Bây giờ bạn có thể:</p>
                    <ul>
                        <li>Đăng nhập vào tài khoản của bạn</li>
                        <li>Kết nối với bạn bè</li>
                        <li>Chia sẻ những khoảnh khắc đáng nhớ</li>
                        <li>Khám phá các tính năng thú vị</li>
                    </ul>
                    <div style="text-align: center; margin: 30px 0;">
                        <p style="color: #667eea; font-size: 18px; font-weight: bold;">
                            Chúc bạn có những trải nghiệm tuyệt vời!
                        </p>
                    </div>
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 25px 0;">
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        Email này được gửi tự động từ %s. Vui lòng không trả lời email này.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(applicationName, displayName, applicationName);
    }

    /**
     * Mask email address for logging (privacy protection).
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return "***" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
