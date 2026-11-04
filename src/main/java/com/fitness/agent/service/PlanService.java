package com.fitness.agent.service;

import com.fitness.agent.dto.PlanRequest;
import com.fitness.agent.dto.PlanView;

import java.util.List;

public interface PlanService {

    PlanView create(Long userId, PlanRequest req);

    /** 按训练目标套内置模板生成一份四周计划。 */
    PlanView generate(Long userId, String name, String goal);

    List<PlanView> list(Long userId);

    PlanView detail(Long userId, Long id);

    void delete(Long userId, Long id);
}
