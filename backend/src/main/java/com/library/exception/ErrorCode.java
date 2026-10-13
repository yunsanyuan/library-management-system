package com.library.exception;

import lombok.Getter;
import lombok.AllArgsConstructor;

/**
 * 错误码枚举，定义所有业务错误码
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    USERNAME_EXISTS(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    BOOK_NOT_FOUND(2001, "图书不存在"),
    BOOK_NO_STOCK(2002, "图书库存不足，无法借阅"),
    BORROW_RECORD_NOT_FOUND(3001, "借阅记录不存在"),
    BORROW_ALREADY_RETURNED(3002, "该借阅记录已归还"),
    BORROW_NOT_YOURS(3003, "只能归还自己借阅的图书"),
    BORROW_DUPLICATE(3004, "您已借阅此书且尚未归还"),
    BORROW_NOT_RETURNED(3005, "借阅中记录不可删除，请先归还"),
    ACCESS_DENIED(4001, "权限不足，禁止访问"),
    TOKEN_INVALID(4002, "Token无效或已过期");

    private final int code;
    private final String message;
}
