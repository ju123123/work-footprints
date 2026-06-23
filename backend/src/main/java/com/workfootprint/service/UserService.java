package com.workfootprint.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.workfootprint.config.WorkFootprintProperties;
import com.workfootprint.entity.UserEntity;
import com.workfootprint.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final WorkFootprintProperties properties;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, WorkFootprintProperties properties) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    public UserEntity findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username));
    }

    public UserEntity ensureDefaultUser() {
        String username = properties.getAuth().getDefaultUsername();
        String password = properties.getAuth().getDefaultPassword();
        UserEntity existing = findByUsername(username);
        if (existing != null) {
            return existing;
        }
        UserEntity u = new UserEntity();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(password));
        userMapper.insert(u);
        return u;
    }

    public UserEntity verifyPassword(String username, String password) {
        UserEntity user = findByUsername(username);
        if (user == null) {
            return null;
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }
        return user;
    }
}

