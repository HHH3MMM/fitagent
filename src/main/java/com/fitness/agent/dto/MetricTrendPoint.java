package com.fitness.agent.dto;

import java.time.LocalDate;

/**
 * 趋势曲线的一个数据点。字段可空：只记录过体重的日期，胸围就是 null，
 * 前端 ECharts 用 connectNulls 把断点连起来。
 */
public record MetricTrendPoint(
        LocalDate date,
        Double weight,
        Double chest,
        Double waist,
        Double hip,
        Double bodyFat
) {
}
