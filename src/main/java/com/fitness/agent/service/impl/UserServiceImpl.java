package com.fitness.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.agent.common.BusinessException;
import com.fitness.agent.dto.AuthResponse;
import com.fitness.agent.dto.LoginRequest;
import com.fitness.agent.dto.RegisterRequest;
import com.fitness.agent.dto.UserInfo;
import com.fitness.agent.entity.User;
import com.fitness.agent.mapper.UserMapper;
import com.fitness.agent.security.JwtUtil;
import com.fitness.agent.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户业务实现。业务规则都写在这里：
 * 注册前查重、密码 BCrypt 加密（绝不明文入库）、登录时比对哈希。
 * 错误统一抛 BusinessException，由全局异常处理器转成 JSON。
 * 数据访问用 MyBatis-Plus：LambdaQueryWrapper 以类型安全的方式拼查询条件。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest req) {
        boolean exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.username())) > 0;
        if (exists) {
            throw new BusinessException(409, "用户名已存在");
        }
        User user = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .nickname(StringUtils.hasText(req.nickname()) ? req.nickname() : req.username())
                .goal(req.goal())
                .height(req.height())
                .build();
        userMapper.insert(user);
        // 注册即登录：直接返回 token，省去一次登录请求
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.username()));
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        return buildAuthResponse(user);
    }

    @Override
    public UserInfo me(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return UserInfo.from(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, UserInfo.from(user));
    }
}
