package com.fitness.agent.dto;

/**
 * 打卡请求：只带备注，打卡日期默认今天（后端决定，防止前端篡改日期）。
 */
public record CheckinRequest(String note) {
}
