package com.fitness.agent.common;

/**
 * 业务异常：Service 层发现"业务上不允许"的情况时抛出，
 * 由 GlobalExceptionHandler 统一转成 JSON 响应，避免到处写 try/catch。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }

    public int getCode() {
        return code;
    }
}
