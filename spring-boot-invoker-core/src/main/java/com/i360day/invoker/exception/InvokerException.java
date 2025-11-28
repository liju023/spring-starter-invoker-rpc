package com.i360day.invoker.exception;


/**
 * <p> @description:   <p>
 * <p> @author: 胡.青牛 <p>
 * <p> @date: 2019/6/11 0011  17:31<p>
 **/
public class InvokerException extends RuntimeException {

    private int status;
    private String message;

    public InvokerException(Throwable cause) {
        super(cause);
        this.status = 500;
        this.message = cause.getMessage();
    }

    public InvokerException(Throwable cause, String message) {
        super(cause);
        this.status = 500;
        this.message = message;
    }

    public InvokerException(InvokerErrorCode errorCode){
        super(errorCode.getMessage());
        this.status = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public InvokerException(String message){
        super(message);
        this.status = 500;
        this.message = message;
    }

    public InvokerException(InvokerErrorCode errorCode, String message){
        super(message);
        this.status = errorCode.getCode();
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
