package com.i360day.invoker;

import com.i360day.invoker.socket.connection.WebSocketConnectionFactory;
import jakarta.websocket.CloseReason;

/**
 * 回复消息监听器
 */
public interface WebSocketReplyToMessageListener{
    
    /**
     * 接收到消息处理
     * @param response  消息响应包
     */
    void handleDelivery(WebSocketResponse response) ;

    /**
     * 关闭监听
     * @param webSocketConnectionFactory
     * @param closeReason
     */
    default void handleShutdownSignal(WebSocketConnectionFactory webSocketConnectionFactory, CloseReason closeReason){

    }

    /**
     * 取消执行
     * @param webSocketConnectionFactory
     * @param sig
     */
    default void handleCancel(WebSocketConnectionFactory webSocketConnectionFactory, Throwable sig){

    }
}
