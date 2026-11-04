package com.fitness.agent.service;

import com.fitness.agent.dto.AiAnalysisResponse;

/**
 * AI 教练服务：以营养师/教练身份与用户对话，必要时调用工具查用户真实数据。
 */
public interface AiCoachService {

    /** 自由对话：AI 结合 RAG 知识库 + 用户真实数据回答。 */
    String chat(Long userId, String message);

    /** 结构化分析：输出 summary/highlights/suggestions/nextStep 四个字段。 */
    AiAnalysisResponse analysis(Long userId);
}
