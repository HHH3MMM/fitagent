package com.fitness.agent.dto;

import com.fitness.agent.entity.Plan;
import com.fitness.agent.entity.PlanItem;

import java.time.LocalDate;
import java.util.List;

/**
 * 计划视图：主表 + 明细拼在一起返回给前端。
 */
public record PlanView(
        Long id,
        String name,
        String goal,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String note,
        List<PlanItem> items
) {
    public static PlanView from(Plan plan, List<PlanItem> items) {
        return new PlanView(plan.getId(), plan.getName(), plan.getGoal(),
                plan.getStartDate(), plan.getEndDate(), plan.getStatus(),
                plan.getNote(), items);
    }
}
