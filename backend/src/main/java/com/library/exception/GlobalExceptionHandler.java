package com.library.exception;

import com.library.common.Result;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 捕获所有异常并转换为统一的Result格式返回
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        return Result.error(ErrorCode.ACCESS_DENIED.getCode(), ErrorCode.ACCESS_DENIED.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst().orElse("参数校验失败");
        return Result.error(400, msg);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst().orElse("参数绑定失败");
        return Result.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(500, "服务器内部错误: " + e.getMessage());
    }
}
