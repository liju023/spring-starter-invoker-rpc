package com.i360day.invoker.exception;


/**
 * <p> @description:   <p>
 * <p> @author: 胡.青牛 <p>
 * <p> @date: 2019/6/11 0011  17:31<p>
 **/
public class WebSocketConnectionErrorException extends RuntimeException {

    public WebSocketConnectionErrorException(Exception exception) {
        super(exception);
    }

    public WebSocketConnectionErrorException(String exception) {
        super(exception);
    }
}
