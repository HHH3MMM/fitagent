package com.fitness.agent.controller;

import com.fitness.agent.common.ApiResponse;
import com.fitness.agent.dto.AuthResponse;
import com.fitness.agent.dto.LoginRequest;
import com.fitness.agent.dto.RegisterRequest;
import com.fitness.agent.dto.UserInfo;
import com.fitness.agent.security.LoginUser;
import com.fitness.agent.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口。Controller 只做三件事：收参数、调 Service、包返回。
 * /me 需要登录：登录用户信息由 Spring Security 从 JWT 解析后注入，无需查库拿身份。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(userService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserInfo> me(@AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(userService.me(user.id()));
    }
}
