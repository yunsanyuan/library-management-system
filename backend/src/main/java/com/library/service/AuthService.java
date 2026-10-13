package com.library.service;

import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.dto.RegisterRequest;
import com.library.entity.User;

/**
 * 认证服务接口
 */
public interface AuthService {
    User register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
