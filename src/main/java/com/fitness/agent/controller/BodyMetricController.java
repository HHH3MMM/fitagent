package com.fitness.agent.controller;

import com.fitness.agent.common.ApiResponse;
import com.fitness.agent.dto.BodyMetricRequest;
import com.fitness.agent.dto.MetricTrendPoint;
import com.fitness.agent.entity.BodyMetric;
import com.fitness.agent.security.LoginUser;
import com.fitness.agent.service.BodyMetricService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 三维记录接口：录入 / 列表 / 更新 / 删除 / 趋势。
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class BodyMetricController {

    private final BodyMetricService bodyMetricService;

    @PostMapping
    public ApiResponse<BodyMetric> record(@AuthenticationPrincipal LoginUser user,
                                          @Valid @RequestBody BodyMetricRequest request) {
        return ApiResponse.ok(bodyMetricService.record(user.id(), request));
    }

    @GetMapping
    public ApiResponse<List<BodyMetric>> list(@AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(bodyMetricService.list(user.id()));
    }

    @GetMapping("/trend")
    public ApiResponse<List<MetricTrendPoint>> trend(@AuthenticationPrincipal LoginUser user,
                                                     @RequestParam(defaultValue = "30") int days) {
        return ApiResponse.ok(bodyMetricService.trend(user.id(), days));
    }

    @PutMapping("/{id}")
    public ApiResponse<BodyMetric> update(@AuthenticationPrincipal LoginUser user,
                                          @PathVariable Long id,
                                          @Valid @RequestBody BodyMetricRequest request) {
        return ApiResponse.ok(bodyMetricService.update(user.id(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long id) {
        bodyMetricService.delete(user.id(), id);
        return ApiResponse.ok();
    }
}
