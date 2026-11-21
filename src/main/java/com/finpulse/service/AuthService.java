package com.finpulse.service;

import com.finpulse.dto.*;
import java.util.Map;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    AuthResponse oauthLogin(OAuthRequest request);
    Map<String, Object> forgotPassword(ForgotPasswordRequest request);
    Map<String, String> resetPassword(ResetPasswordRequest request);
    Map<String, Object> getProfile();
    AuthResponse updateProfile(ProfileUpdateRequest request);
}
