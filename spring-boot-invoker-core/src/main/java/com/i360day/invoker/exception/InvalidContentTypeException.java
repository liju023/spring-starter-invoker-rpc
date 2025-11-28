package com.i360day.invoker.exception;


/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/11 0011  17:31<p>
 **/
public class InvalidContentTypeException extends RuntimeException {

    public InvalidContentTypeException(InvokerErrorCode errorCode){
        super(errorCode.getMessage());
    }

    public InvalidContentTypeException(String message){
        super(message);
    }

    public InvalidContentTypeException(InvokerErrorCode errorCode, String message){
        super(message);
    }
}
