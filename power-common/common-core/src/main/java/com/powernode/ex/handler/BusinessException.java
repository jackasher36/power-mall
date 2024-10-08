package com.powernode.ex.handler;

/**
 * 自定义业务异常类
 */
public class BusinessException extends RuntimeException{
    public BusinessException(String message) {
        super(message);
    }
}
