package com.library.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化运行器
 * 应用启动时检查并确保管理员账号存在，密码正确
 */
@Slf4j
@Component
public class DataInitRunner implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitRunner(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            ensureUser("admin", "admin123", "admin");
            ensureUser("superadmin", "super123", "super_admin");
        } catch (Exception e) {
            log.error("内置账号初始化失败", e);
        }
    }

    private void ensureUser(String username, String password, String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            userMapper.insert(user);
            log.info("内置账号创建成功: {} / {}", username, password);
        } else {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(password));
                userMapper.updateById(user);
                log.info("账号 {} 密码已更新", username);
            }
            if (!role.equals(user.getRole())) {
                user.setRole(role);
                userMapper.updateById(user);
                log.info("账号 {} 角色已更新为 {}", username, role);
            }
        }
    }
}
