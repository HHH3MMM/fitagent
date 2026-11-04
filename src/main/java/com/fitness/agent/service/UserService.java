package com.fitness.agent.service;

import com.fitness.agent.dto.AuthResponse;
import com.fitness.agent.dto.LoginRequest;
import com.fitness.agent.dto.RegisterRequest;
import com.fitness.agent.dto.UserInfo;

/**
 * 用户业务接口。接口 + 实现类的拆分是国内企业的主流写法：
 * 好处是便于单元测试 mock、便于日后替换实现，也是面试常被问到的点。
 */
public interface UserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserInfo me(Long userId);
}
