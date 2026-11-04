package com.fitness.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求。@NotBlank/@Size 是 Bean Validation：
 * 请求参数在进入 Service 前就被校验，非法输入直接返回 400。
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度 3-50")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度 6-32")
        String password,

        String nickname,
        String goal,
        Double height
) {
}
