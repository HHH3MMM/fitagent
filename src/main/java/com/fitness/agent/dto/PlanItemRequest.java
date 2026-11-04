package com.fitness.agent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 计划明细请求：周几 + 动作 + 组数/次数/重量。
 */
public record PlanItemRequest(
        @NotNull(message = "训练日不能为空")
        @Min(value = 1, message = "dayOfWeek 范围 1-7")
        @Max(value = 7, message = "dayOfWeek 范围 1-7")
        Integer dayOfWeek,
        @NotBlank(message = "动作名称不能为空")
        String exercise,
        Integer sets,
        Integer reps,
        Double weight,
        String note
) {
}
