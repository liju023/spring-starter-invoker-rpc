package com.i360day.invoker.exception;


/**
 * <p> @description:   <p>
 * <p> @author: 胡.青牛 <p>
 * <p> @date: 2019/6/11 0011  17:31<p>
 **/
public class InvalidAddressException extends RuntimeException {

    public InvalidAddressException(InvokerErrorCode errorCode){
        super(errorCode.getMessage());
    }

    public InvalidAddressException(String message){
        super(message);
    }

    public InvalidAddressException(InvokerErrorCode errorCode, String message){
        super(message);
    }
}
