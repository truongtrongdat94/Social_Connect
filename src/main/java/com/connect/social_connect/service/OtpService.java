package com.connect.social_connect.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connect.social_connect.domain.OtpVerification;
import com.connect.social_connect.repository.OtpVerificationRepository;

@Service
public class OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final SecureRandom secureRandom;

    @Value("${app.otp.expiration-minutes:5}")
    private int expirationMinutes;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.otp.lockout-minutes:15}")
    private int lockoutMinutes;

    @Value("${app.otp.max-resend:3}")
    private int maxResend;

    @Value("${app.otp.resend-cooldown-minutes:30}")
    private int resendCooldownMinutes;

    public OtpService(OtpVerificationRepository otpVerificationRepository) {
        this.otpVerificationRepository = otpVerificationRepository;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generate a 6-digit OTP and save to database.
     * If an OTP already exists for the email, it will be invalidated and replaced.
     */
    @Transactional
    public String generateOtp(String email) {
        // Delete any existing OTP for this email
        otpVerificationRepository.deleteByEmail(email);

        // Generate 6-digit OTP
        String otpCode = generateOtpCode();

        // Create and save new OTP verification
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(email);
        otpVerification.setOtpCode(otpCode);
        otpVerification.setExpiresAt(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES));
        otpVerification.setAttemptCount(0);
        otpVerification.setResendCount(0);
        otpVerification.setCreatedAt(Instant.now());
        otpVerification.setLockedUntil(null);

        otpVerificationRepository.save(otpVerification);

        return otpCode;
    }

    /**
     * Verify the OTP code for a given email.
     * Increments attempt count on failure.
     */
    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        Optional<OtpVerification> optionalOtp = otpVerificationRepository.findByEmail(email);

        if (optionalOtp.isEmpty()) {
            return false;
        }

        OtpVerification otpVerification = optionalOtp.get();

        // Check if locked
        if (isLocked(otpVerification)) {
            return false;
        }

        // Check if expired
        if (isExpired(otpVerification)) {
            return false;
        }

        // Check if OTP matches
        if (!otpVerification.getOtpCode().equals(otpCode)) {
            // Increment attempt count
            otpVerification.setAttemptCount(otpVerification.getAttemptCount() + 1);

            // Lock if max attempts reached
            if (otpVerification.getAttemptCount() >= maxAttempts) {
                otpVerification.setLockedUntil(Instant.now().plus(lockoutMinutes, ChronoUnit.MINUTES));
            }

            otpVerificationRepository.save(otpVerification);
            return false;
        }

        // OTP is valid - delete it (single-use)
        otpVerificationRepository.deleteByEmail(email);
        return true;
    }

     //Check if OTP verification is locked due to too many failed attempts.
    public boolean isOtpLocked(String email) {
        Optional<OtpVerification> optionalOtp = otpVerificationRepository.findByEmail(email);

        if (optionalOtp.isEmpty()) {
            return false;
        }

        return isLocked(optionalOtp.get());
    }

    /**
     * Resend OTP by generating a new one and invalidating the previous.
     * Increments resend count.
     */
    @Transactional
    public String resendOtp(String email) {
        Optional<OtpVerification> optionalOtp = otpVerificationRepository.findByEmail(email);

        if (optionalOtp.isEmpty()) {
            // No existing OTP, generate new one
            return generateOtp(email);
        }

        OtpVerification otpVerification = optionalOtp.get();

        // Check if resend limit reached
        if (otpVerification.getResendCount() >= maxResend) {
            throw new IllegalStateException("Resend limit reached. Please wait before requesting again.");
        }

        // Generate new OTP code
        String newOtpCode = generateOtpCode();

        // Update existing record with new OTP
        otpVerification.setOtpCode(newOtpCode);
        otpVerification.setExpiresAt(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES));
        otpVerification.setAttemptCount(0);
        otpVerification.setResendCount(otpVerification.getResendCount() + 1);
        otpVerification.setLockedUntil(null);

        otpVerificationRepository.save(otpVerification);

        return newOtpCode;
    }

     //Get the remaining number of resend attempts for an email.
    public int getRemainingResendAttempts(String email) {
        Optional<OtpVerification> optionalOtp = otpVerificationRepository.findByEmail(email);

        if (optionalOtp.isEmpty()) {
            return maxResend;
        }

        int remaining = maxResend - optionalOtp.get().getResendCount();
        return Math.max(0, remaining);
    }

     //Generate a 6-digit numeric OTP code.
    private String generateOtpCode() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

     //Check if the OTP verification is locked.
    private boolean isLocked(OtpVerification otpVerification) {
        Instant lockedUntil = otpVerification.getLockedUntil();
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

     //Check if the OTP has expired.
    private boolean isExpired(OtpVerification otpVerification) {
        return Instant.now().isAfter(otpVerification.getExpiresAt());
    }
}
