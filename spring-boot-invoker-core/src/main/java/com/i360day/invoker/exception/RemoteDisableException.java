package com.i360day.invoker.exception;


/**
 * <p> @description:   <p>
 * <p> @author: 胡.青牛 <p>
 * <p> @date: 2019/6/11 0011  17:31<p>
 **/
public class RemoteDisableException extends RuntimeException {

    public RemoteDisableException(InvokerErrorCode errorCode){
        super(errorCode.getMessage());
    }

    public RemoteDisableException(String message){
        super(message);
    }

    public RemoteDisableException(InvokerErrorCode errorCode, String message){
        super(message);
    }
}
