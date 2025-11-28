package com.i360day.invoker.exception;


/**
 * <p> @description:   <p>
 * <p> @author: 胡.青牛 <p>
 * <p> @date: 2019/6/11 0011  17:31<p>
 **/
public class RemoteClientHystrixException extends RuntimeException {

    private int status;
    private String message;

    public RemoteClientHystrixException(Throwable cause) {
        super(cause);
        this.status = 500;
        this.message = cause.getMessage();
    }

    public RemoteClientHystrixException(Throwable cause, String message) {
        super(cause);
        this.status = 500;
        this.message = message;
    }

    public RemoteClientHystrixException(InvokerErrorCode errorCode){
        super(errorCode.getMessage());
        this.status = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public RemoteClientHystrixException(String message){
        super(message);
        this.status = 500;
        this.message = message;
    }

    public RemoteClientHystrixException(InvokerErrorCode errorCode, String message){
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
