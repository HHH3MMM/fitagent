package com.fitness.agent.common;

/**
 * 统一响应体：所有接口都返回 {code, message, data}
 * code=0 表示成功；非 0 为业务错误码（与 HTTP 状态码保持一致便于排查）
 */
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
