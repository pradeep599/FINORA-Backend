package com.finpulse.service.impl;

import com.finpulse.dto.*;
import com.finpulse.entity.Account;
import com.finpulse.entity.User;
import com.finpulse.exception.BusinessRuleException;
import com.finpulse.exception.ResourceNotFoundException;
import com.finpulse.repository.AccountRepository;
import com.finpulse.repository.UserRepository;
import com.finpulse.security.JwtUtil;
import com.finpulse.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public AuthServiceImpl(UserRepository userRepository, AccountRepository accountRepository,
                           PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email.toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("An account with this email already exists");
        }
        User user = userRepository.save(new User(req.fullName.trim(), email,
                passwordEncoder.encode(req.password), req.currency));
        accountRepository.save(new Account(user, "Primary Bank Account", "BANK", BigDecimal.ZERO));
        return new AuthResponse(jwtUtil.generateToken(user.getEmail()), user.getId(),
                user.getFullName(), user.getEmail(), user.getCurrency());
    }

    @Override
    public AuthResponse login(AuthRequest req) {
        User user = userRepository.findByEmail(req.email.toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return new AuthResponse(jwtUtil.generateToken(user.getEmail()), user.getId(),
                user.getFullName(), user.getEmail(), user.getCurrency());
    }

    @Override
    @Transactional
    public AuthResponse oauthLogin(OAuthRequest req) {
        String email = req.email.toLowerCase().trim();
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String displayName = (req.fullName != null && !req.fullName.isBlank())
                    ? req.fullName.trim()
                    : email.substring(0, email.indexOf('@'));
            User u = new User(displayName, email, passwordEncoder.encode(UUID.randomUUID().toString()), "INR");
            u.setAuthProvider(req.provider.toUpperCase());
            u = userRepository.save(u);
            accountRepository.save(new Account(u, "Primary Bank Account", "BANK", BigDecimal.ZERO));
            return u;
        });
        return new AuthResponse(jwtUtil.generateToken(user.getEmail()), user.getId(),
                user.getFullName(), user.getEmail(), user.getCurrency());
    }

    @Override
    @Transactional
    public Map<String, Object> forgotPassword(ForgotPasswordRequest req) {
        String email = req.email.toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + email));

        String code = String.format("%06d", new SecureRandom().nextInt(1000000));
        user.setResetToken(code);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        boolean emailSent = false;
        if (mailSender != null && mailUsername != null && !mailUsername.isBlank()) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(mailUsername);
                msg.setTo(user.getEmail());
                msg.setSubject("Finora — Password Reset Verification Code");
                msg.setText("Hi " + user.getFullName() + ",\n\nYour Finora password reset code is: " + code
                        + "\n\nThis code expires in 15 minutes. If you didn't request this, ignore this email.");
                mailSender.send(msg);
                emailSent = true;
            } catch (Exception ignored) {}
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("emailSent", emailSent);
        res.put("email", user.getEmail());
        if (emailSent) {
            res.put("message", "Reset verification code sent to " + user.getEmail() + ". Please check your inbox.");
        } else {
            res.put("message", "SMTP mail not configured in application.properties — using demo reset code below.");
            res.put("demoResetCode", code);
        }
        return res;
    }

    @Override
    @Transactional
    public Map<String, String> resetPassword(ResetPasswordRequest req) {
        User user = userRepository.findByEmail(req.email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (user.getResetToken() == null || !user.getResetToken().equals(req.token.trim())) {
            throw new BusinessRuleException("Invalid reset verification code");
        }
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Reset code has expired. Please request a new one.");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        return Map.of("message", "Password updated successfully! You can now sign in with your new password.");
    }

    @Override
    public Map<String, Object> getProfile() {
        User u = currentUser();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", u.getId());
        m.put("fullName", u.getFullName());
        m.put("email", u.getEmail());
        m.put("currency", u.getCurrency());
        m.put("authProvider", u.getAuthProvider());
        m.put("createdAt", u.getCreatedAt().toString());
        return m;
    }

    @Override
    @Transactional
    public AuthResponse updateProfile(ProfileUpdateRequest req) {
        User u = currentUser();
        u.setFullName(req.fullName.trim());
        u.setCurrency(req.currency.toUpperCase());
        if (req.newPassword != null && !req.newPassword.isBlank()) {
            if (req.newPassword.length() < 6) {
                throw new BusinessRuleException("New password must be at least 6 characters");
            }
            if ("LOCAL".equalsIgnoreCase(u.getAuthProvider())
                    && (req.currentPassword == null || !passwordEncoder.matches(req.currentPassword, u.getPasswordHash()))) {
                throw new BusinessRuleException("Current password is incorrect");
            }
            u.setPasswordHash(passwordEncoder.encode(req.newPassword));
        }
        u = userRepository.save(u);
        return new AuthResponse(jwtUtil.generateToken(u.getEmail()), u.getId(),
                u.getFullName(), u.getEmail(), u.getCurrency());
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
