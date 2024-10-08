package com.powernode.ex.handler;

import com.powernode.constant.BusinessEnum;
import com.powernode.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandle {

    @ExceptionHandler(BusinessException.class)
    public Result<String> businessException(BusinessException e) {
        log.error(e.getMessage());
        return Result.fail(BusinessEnum.OPERATION_FAIL.getCode(),e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> runtimeException(RuntimeException e) {
        log.error(e.getMessage());
        return Result.fail(BusinessEnum.SERVER_INNER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> accessDeniedException(AccessDeniedException e) {
        log.error(e.getMessage());
        throw e;
    }
}
