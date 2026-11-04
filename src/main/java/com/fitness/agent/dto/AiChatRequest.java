package com.fitness.agent.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * AI 对话请求。
 */
public record AiChatRequest(
        @NotBlank(message = "消息不能为空")
        String message
) {
}
