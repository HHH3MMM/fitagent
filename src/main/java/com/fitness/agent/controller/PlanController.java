package com.fitness.agent.controller;

import com.fitness.agent.common.ApiResponse;
import com.fitness.agent.dto.PlanRequest;
import com.fitness.agent.dto.PlanView;
import com.fitness.agent.security.LoginUser;
import com.fitness.agent.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 训练计划接口：自定义创建 / 模板生成 / 列表 / 详情 / 删除。
 */
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    public ApiResponse<PlanView> create(@AuthenticationPrincipal LoginUser user,
                                        @Valid @RequestBody PlanRequest request) {
        return ApiResponse.ok(planService.create(user.id(), request));
    }

    @PostMapping("/generate")
    public ApiResponse<PlanView> generate(@AuthenticationPrincipal LoginUser user,
                                          @RequestParam String name,
                                          @RequestParam String goal) {
        return ApiResponse.ok(planService.generate(user.id(), name, goal));
    }

    @GetMapping
    public ApiResponse<List<PlanView>> list(@AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(planService.list(user.id()));
    }

    @GetMapping("/{id}")
    public ApiResponse<PlanView> detail(@AuthenticationPrincipal LoginUser user, @PathVariable Long id) {
        return ApiResponse.ok(planService.detail(user.id(), id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long id) {
        planService.delete(user.id(), id);
        return ApiResponse.ok();
    }
}
