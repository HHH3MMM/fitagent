package com.fitness.agent.dto;

/**
 * 登录/注册成功后的响应：JWT 令牌 + 用户信息。
 * 前端拿到 token 后存起来，之后每个请求带 Authorization: Bearer <token>。
 */
public record AuthResponse(String token, UserInfo user) {
}
