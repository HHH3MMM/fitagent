package com.fitness.agent.security;

/**
 * 已登录用户的主体会放入 SecurityContext。
 * 控制器方法上用 @AuthenticationPrincipal LoginUser 即可拿到当前用户，
 * 不用每次查库、不用自己解析 token。
 */
public record LoginUser(Long id, String username) {
}
