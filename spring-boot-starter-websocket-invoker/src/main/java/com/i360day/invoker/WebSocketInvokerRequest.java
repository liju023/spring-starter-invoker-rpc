package com.i360day.invoker;

import com.i360day.invoker.codes.WebSocketMessageSerialize;
import com.i360day.invoker.common.InvokerConstant;
import com.i360day.invoker.exception.InvokerException;
import com.i360day.invoker.exception.InvokerTimeoutException;
import com.i360day.invoker.http.Response;
import com.i360day.invoker.properties.InvokerProperties;
import com.i360day.invoker.properties.InvokerWebSocketProperties;
import com.i360day.invoker.proxy.TargetProxy;
import com.i360day.invoker.request.InvokerRequest;
import com.i360day.invoker.socket.connection.SimpleWebSocketConnectionFactory;
import com.i360day.invoker.socket.connection.WebSocketConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.http.HttpStatus;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 获取到上下文socket manage
 */
public class WebSocketInvokerRequest implements InvokerRequest, WebSocketReplyToMessageListener, DisposableBean {
    private Logger logger = LoggerFactory.getLogger(WebSocketInvokerRequest.class);
    /**
     * webSocket 序列化
     */
    private WebSocketMessageSerialize webSocketMessageSerialize;
    /**
     * 配置
     */
    private InvokerProperties invokerProperties;
    /**
     * webSocket config
     */
    private InvokerWebSocketProperties invokerWebSocketProperties;
    /**
     * 等待请求回复
     */
    private final Map<String, MessagePendingReply<WebSocketResponse>> replyHolder = new ConcurrentHashMap<>();
    /**
     * 多个webSocket连接工厂
     */
    private Map<String, WebSocketConnectionFactory> multiWebsocketConnectionFactoryMap = new ConcurrentHashMap<>();
    /**
     * 侦听消息回复器容器
     */
    private Map<WebSocketConnectionFactory, WebSocketDirectReplyToMessageListenerContainer> replyToMessageListenerContainerMap = new ConcurrentHashMap<>();

    /**
     * WebSocketHttpInvokerRequest
     *
     * @param invokerProperties
     * @see WebSocketInvokerClientFactoryBean#WebSocketInvokerClientFactoryBean
     */
    public WebSocketInvokerRequest(WebSocketMessageSerialize webSocketMessageSerialize, InvokerProperties invokerProperties, InvokerWebSocketProperties invokerWebSocketProperties) {
        this.webSocketMessageSerialize = webSocketMessageSerialize;
        this.invokerProperties = invokerProperties;
        this.invokerWebSocketProperties = invokerWebSocketProperties;
    }


    /**
     * 获取websocket 链接
     *
     * @param requestTemplate
     * @return
     */
    private WebSocketConnectionFactory getWebsocketConnectionFactory(RequestTemplate requestTemplate) {
        String webSocketConnectionFactoryKey = requestTemplate.getUri().getAuthority();
        WebSocketConnectionFactory webSocketConnectionFactory = this.multiWebsocketConnectionFactoryMap.get(webSocketConnectionFactoryKey);
        if(webSocketConnectionFactory == null){

            webSocketConnectionFactory = this.multiWebsocketConnectionFactoryMap.get(webSocketConnectionFactoryKey);
            if(webSocketConnectionFactory == null){
                synchronized (multiWebsocketConnectionFactoryMap) {
                    TargetProxy targetProxy = requestTemplate.getTargetProxy();

                    return multiWebsocketConnectionFactoryMap.computeIfAbsent(webSocketConnectionFactoryKey, o -> {
                        try {
                            SimpleWebSocketConnectionFactory simpleWebSocketConnectionFactory = new SimpleWebSocketConnectionFactory(this.invokerWebSocketProperties, requestTemplate.getRequestBasePath());
                            simpleWebSocketConnectionFactory.setClientEndpointConfig(
                                    ClientEndpointConfig.Builder.create().configurator(new ClientEndpointConfig.Configurator() {
                                        @Override
                                        public void beforeRequest(Map<String, List<String>> headers) {
                                            headers.put("username", Arrays.asList(targetProxy.getAnnotationAttributeAsString("username")));
                                            headers.put("password", Arrays.asList(targetProxy.getAnnotationAttributeAsString("password")));
                                            headers.put("Client-Id", Arrays.asList(UUID.randomUUID().toString()));
                                        }
                                    }).build()
                            );
                            simpleWebSocketConnectionFactory.afterPropertiesSet();
                            simpleWebSocketConnectionFactory.start();
                            return simpleWebSocketConnectionFactory;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }

        return webSocketConnectionFactory;
    }

    /**
     * 执行 rpc 请求
     *
     * @param requestTemplate
     * @return
     * @throws IOException
     */
    @Override
    public Response executor(RequestTemplate requestTemplate) throws IOException {
        //创建websocket工厂
        WebSocketConnectionFactory websocketConnectionFactory = getWebsocketConnectionFactory(requestTemplate);

        //获取消息回复监听容器
        WebSocketDirectReplyToMessageListenerContainer replyToMessageListenerContainer = replyToMessageListenerContainerMap.get(websocketConnectionFactory);
        if (replyToMessageListenerContainer == null) {
            synchronized (replyToMessageListenerContainerMap) {
                replyToMessageListenerContainer = replyToMessageListenerContainerMap.computeIfAbsent(websocketConnectionFactory, o -> {
                    try {
                        WebSocketDirectReplyToMessageListenerContainer messageListenerContainer = new WebSocketDirectReplyToMessageListenerContainer(webSocketMessageSerialize, websocketConnectionFactory);
                        messageListenerContainer.addListener(this);
                        return messageListenerContainer;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        //send message
        return sendMessage(requestTemplate, replyToMessageListenerContainer);
    }

    /**
     * 发送client请求
     *
     * @param requestTemplate
     * @param replyToMessageListenerContainer
     * @return
     */
    private Response sendMessage(RequestTemplate requestTemplate, WebSocketDirectReplyToMessageListenerContainer replyToMessageListenerContainer) {
        //request
        WebSocketRequest webSocketRequest = WebSocketRequest.getRequestToWebSocketRequest(requestTemplate);

        try {
            //等待消息回复对象
            MessagePendingReply<WebSocketResponse> messagePendingReply = replyHolder.computeIfAbsent(webSocketRequest.getRequestId(), (o) -> {
                return new MessagePendingReply<>(replyToMessageListenerContainer.getWebSocketConnectionFactory().toString());
            });

            //发送client请求，异步等待结果
            return replyToMessageListenerContainer.execute((session) -> {
                try {
                    session.getAsyncRemote().sendBinary(ByteBuffer.wrap(webSocketMessageSerialize.encode(webSocketRequest)));

                    //返回信息
                    WebSocketResponse response = messagePendingReply.get(invokerProperties.getRequest().getReadTimeout(), TimeUnit.MILLISECONDS);

                    return Response.Builder.create()
                            .body(new ByteArrayInputStream(response.getBody()))
                            .status(response.getHttpStatus().value())
                            .setContextType(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER)
                            .build();

                } catch (Exception e) {
                    byte[] bytes = Optional.ofNullable(e.getMessage()).orElse("").getBytes(StandardCharsets.UTF_8);
                    return Response.Builder.create()
                            .body(new ByteArrayInputStream(bytes))
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .setContextType(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER)
                            .build();
                }
            });
        } catch (Exception ex) {
            if (ex instanceof TimeoutException) {
                throw new InvokerTimeoutException(ex);
            }
            throw new InvokerException(ex);
        } finally {
            replyHolder.remove(webSocketRequest.getRequestId());
        }
    }

    /**
     * 消息回复
     *
     * @param response 消息响应包
     * @see WebSocketInvokerRequest#replyHolder
     */
    @Override
    public void handleDelivery(WebSocketResponse response) {
        try {
            Optional.ofNullable(replyHolder.get(response.getResponseId())).ifPresent(reply -> {
                reply.reply(response);
            });
        } catch (Exception ex) {
            logger.warn("redis request Subscription message processing failed {}", ex.getMessage());
        }
    }

    /**
     * 停止监听
     *
     * @param webSocketConnectionFactory
     * @param closeReason
     */
    @Override
    public void handleShutdownSignal(WebSocketConnectionFactory webSocketConnectionFactory, CloseReason closeReason) {
        replyHolder.values().stream()
                .filter(f -> f.getContainerId().equals(webSocketConnectionFactory.toString()))
                .forEach(item -> item.completeExceptionally(new ClosedChannelException()));
    }

    /**
     * 取消请求
     *
     * @param webSocketConnectionFactory
     * @param sig
     */
    @Override
    public void handleCancel(WebSocketConnectionFactory webSocketConnectionFactory, Throwable sig) {
        replyHolder.values().stream()
                .filter(f -> f.getContainerId().equals(webSocketConnectionFactory.toString()))
                .forEach(item -> item.completeExceptionally(sig));
    }

    /**
     * 销毁
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        //回复消息容器
        multiWebsocketConnectionFactoryMap.values().forEach(container -> {
            try {
                container.destroy();
            } catch (Exception ex) {
                //ignore ex
            }
        });

        //多维度链接工厂
        replyToMessageListenerContainerMap.values().forEach(connectionFactory -> {
            if (connectionFactory instanceof SmartLifecycle) {
                try {
                    ((SmartLifecycle) connectionFactory).stop();
                } catch (Exception ex) {
                    //ignore ex
                }
            }
        });

        //消息回复
        replyHolder.values().forEach(replyHolder -> replyHolder.completeExceptionally(new InterruptedException()));
    }
}
