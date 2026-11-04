package com.fitness.agent.dto;

import com.fitness.agent.entity.User;

import java.time.LocalDateTime;

/**
 * 返回给前端的用户信息（绝不包含密码哈希）。
 * from() 做 Entity -> DTO 转换：实体不直接暴露给前端，避免密码等敏感字段泄漏。
 */
public record UserInfo(
        Long id,
        String username,
        String nickname,
        String goal,
        Double height,
        LocalDateTime createdAt
) {
    public static UserInfo from(User u) {
        return new UserInfo(u.getId(), u.getUsername(), u.getNickname(), u.getGoal(), u.getHeight(), u.getCreatedAt());
    }
}
