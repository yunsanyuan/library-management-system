package com.library.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.dto.ChangePasswordRequest;
import com.library.dto.UserVO;
import com.library.entity.User;

public interface UserService {
    Page<UserVO> listUsers(int page, int size, String keyword);
    UserVO updateUserRole(Long id, String role, String operatorRole);
    void deleteUser(Long id, String operatorRole);
    void changePassword(Long userId, ChangePasswordRequest request);
    void resetPassword(Long id, String newPassword, String operatorRole);
    UserVO changeUsername(Long userId, String newUsername);
    UserVO getUserById(Long userId);
    UserVO updateAvatar(Long userId, String avatarUrl);
    UserVO updateBio(Long userId, String bio);
    UserVO getPublicProfile(Long userId);
}
