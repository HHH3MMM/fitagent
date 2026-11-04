package com.fitness.agent.controller;

import com.fitness.agent.common.ApiResponse;
import com.fitness.agent.dto.CheckinRequest;
import com.fitness.agent.entity.Checkin;
import com.fitness.agent.security.LoginUser;
import com.fitness.agent.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 打卡接口。Controller 依旧只做：收参 → 调 Service → 包返回。
 * 当前用户从 @AuthenticationPrincipal 拿（JWT 过滤器已解析好），
 * 全程不出现"从 token 里手动取 id"的代码——这是 Spring Security 的职责。
 */
@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @PostMapping
    public ApiResponse<Checkin> checkin(@AuthenticationPrincipal LoginUser user,
                                        @RequestBody(required = false) CheckinRequest request) {
        return ApiResponse.ok(checkinService.checkin(user.id(),
                request == null ? new CheckinRequest(null) : request));
    }

    @GetMapping
    public ApiResponse<List<Checkin>> list(@AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(checkinService.list(user.id()));
    }

    @GetMapping("/streak")
    public ApiResponse<Integer> streak(@AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(checkinService.streak(user.id()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long id) {
        checkinService.delete(user.id(), id);
        return ApiResponse.ok();
    }
}
