package com.library.controller;

import com.library.common.Result;
import com.library.dto.*;
import com.library.entity.User;
import com.library.service.AuthService;
import com.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 * 处理用户注册、登录、个人信息、修改账号名、修改密码
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户注册、登录和个人信息接口")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * 用户注册，角色默认为user
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户，角色默认为user")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return Result.success(user);
    }

    /**
     * 用户登录，返回JWT Token和角色
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "登录后返回JWT Token和用户角色")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "当前用户信息", description = "获取当前登录用户信息")
    public Result<UserVO> getCurrentUser(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(userService.getUserById(userId));
    }

    /**
     * 修改当前用户账号名
     */
    @PutMapping("/username")
    @Operation(summary = "修改账号名", description = "当前用户修改自己的账号名")
    public Result<UserVO> changeUsername(@RequestBody java.util.Map<String, String> body,
                                         HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(userService.changeUsername(userId, body.get("username")));
    }

    /**
     * 修改当前用户密码
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "当前用户修改密码，需校验旧密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                       HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userService.changePassword(userId, request);
        return Result.success();
    }

    @PutMapping("/avatar")
    @Operation(summary = "修改头像", description = "当前用户修改头像URL，限制Base64大小不超过60KB")
    public Result<UserVO> updateAvatar(@RequestBody java.util.Map<String, String> body,
                                       HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String avatarUrl = body.get("avatarUrl");
        if (avatarUrl != null && avatarUrl.length() > 60000) {
            throw new com.library.exception.BusinessException(400, "图片过大，请上传2MB以内的图片");
        }
        return Result.success(userService.updateAvatar(userId, avatarUrl));
    }

    @PutMapping("/bio")
    @Operation(summary = "修改简介", description = "当前用户修改个人简介")
    public Result<UserVO> updateBio(@RequestBody java.util.Map<String, String> body,
                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(userService.updateBio(userId, body.get("bio")));
    }
}
