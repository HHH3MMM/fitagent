package com.fitness.agent.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 三维记录请求。recordDate 必填；各围度可空（今天只量了体重就只填体重）。
 */
public record BodyMetricRequest(
        @NotNull(message = "记录日期不能为空")
        LocalDate recordDate,
        Double weight,
        Double chest,
        Double waist,
        Double hip,
        Double bodyFat,
        String note
) {
}
