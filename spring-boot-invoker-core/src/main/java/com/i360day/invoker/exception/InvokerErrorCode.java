package com.i360day.invoker.exception;

/**
 * <p> @description:   <p>
 * <p> @author: 胡.青牛 <p>
 * <p> @date: 2019/6/12 0012  11:52<p>
 **/
public enum InvokerErrorCode {
    error_401(401,"You can't access it directly by URL."),
    error_500(500,"Internal errors at service runtime")
    ;
    InvokerErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
