package com.library.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.dto.RegisterRequest;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.exception.ErrorCode;
import com.library.mapper.UserMapper;
import com.library.security.JwtTokenProvider;
import com.library.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 * 处理用户注册和登录逻辑
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 用户注册
     * 校验用户名唯一性，密码BCrypt加密存储，角色默认user
     */
    @Override
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectOne(wrapper) != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("user");
        userMapper.insert(user);

        // 返回时不携带密码
        user.setPassword(null);
        return user;
    }

    /**
     * 用户登录
     * 校验用户名密码，生成JWT Token返回
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 生成JWT Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getRole(), user.getUsername(), user.getAvatarUrl());
    }
}
