package com.library.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.dto.ChangePasswordRequest;
import com.library.dto.UserVO;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.exception.ErrorCode;
import com.library.mapper.UserMapper;
import com.library.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现
 * 处理用户列表、角色修改、密码修改等
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<UserVO> listUsers(int page, int size, String keyword) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(pageParam, wrapper);

        Page<UserVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    @Override
    public UserVO updateUserRole(Long id, String role, String operatorRole) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (!"super_admin".equals(operatorRole) && ("admin".equals(role) || "super_admin".equals(role))) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        user.setRole(role);
        userMapper.updateById(user);
        return toVO(user);
    }

    @Override
    public void deleteUser(Long id, String operatorRole) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (!"super_admin".equals(operatorRole) && ("admin".equals(user.getRole()) || "super_admin".equals(user.getRole()))) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        userMapper.deleteById(id);
    }

    /**
     * 修改当前用户密码，需校验旧密码
     */
    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    /**
     * 管理员重置用户密码
     */
    @Override
    public void resetPassword(Long id, String newPassword, String operatorRole) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (!"super_admin".equals(operatorRole) && "super_admin".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    /**
     * 修改当前用户用户名，校验唯一性
     */
    @Override
    public UserVO changeUsername(Long userId, String newUsername) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (!StringUtils.hasText(newUsername) || newUsername.length() < 3 || newUsername.length() > 50) {
            throw new BusinessException(400, "用户名长度需3-50");
        }
        // 校验用户名唯一性
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, newUsername).ne(User::getId, userId);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        user.setUsername(newUsername);
        userMapper.updateById(user);
        return toVO(user);
    }

    @Override
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        return toVO(user);
    }

    @Override
    public UserVO updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        user.setAvatarUrl(avatarUrl);
        userMapper.updateById(user);
        return toVO(user);
    }

    @Override
    public UserVO updateBio(Long userId, String bio) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        user.setBio(bio);
        userMapper.updateById(user);
        return toVO(user);
    }

    @Override
    public UserVO getPublicProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        return toVO(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setBio(user.getBio());
        vo.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().toString() : null);
        return vo;
    }
}
