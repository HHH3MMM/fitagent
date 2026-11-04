package com.fitness.agent.dto;

/**
 * AI 结构化分析结果（AI 教练/营养师视角）。
 * 字段由大模型按提示词输出，前端直接渲染。
 */
public record AiAnalysisResponse(
        String summary,
        String highlights,
        String suggestions,
        String nextStep
) {
}
