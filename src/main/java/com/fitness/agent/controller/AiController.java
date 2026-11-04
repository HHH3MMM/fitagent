package com.fitness.agent.controller;

import com.fitness.agent.common.ApiResponse;
import com.fitness.agent.dto.AiAnalysisResponse;
import com.fitness.agent.dto.AiChatRequest;
import com.fitness.agent.dto.AiChatResponse;
import com.fitness.agent.security.LoginUser;
import com.fitness.agent.service.AiCoachService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 教练接口。
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiCoachService aiCoachService;

    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(@AuthenticationPrincipal LoginUser user,
                                            @Valid @RequestBody AiChatRequest request) {
        String reply = aiCoachService.chat(user.id(), request.message());
        return ApiResponse.ok(new AiChatResponse(reply));
    }

    @PostMapping("/analysis")
    public ApiResponse<AiAnalysisResponse> analysis(@AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(aiCoachService.analysis(user.id()));
    }
}
