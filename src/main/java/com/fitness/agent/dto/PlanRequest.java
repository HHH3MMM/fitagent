package com.fitness.agent.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建计划请求：主表信息 + 明细列表。
 */
public record PlanRequest(
        @NotBlank(message = "计划名称不能为空")
        String name,
        String goal,
        LocalDate startDate,
        LocalDate endDate,
        String note,
        @Valid
        List<PlanItemRequest> items
) {
}
