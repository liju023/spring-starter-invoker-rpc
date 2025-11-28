package com.i360day.invoker;

import com.i360day.invoker.common.InvokerUrlUtils;
import com.i360day.invoker.properties.InvokerProperties;
import com.i360day.invoker.proxy.TargetProxy;
import com.rabbitmq.client.Address;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class AmqpInvokerSupportAbstractRequest implements AmqpMessageListener, AmqpShutdownListener{

    /**
     * 多维度链接工厂
     */
    private final Map<String, ConnectionFactory> multiTenancyConnectionFactory = new ConcurrentHashMap<>();
    /**
     * 默认链接工厂
     */
    private final ConnectionFactory defaultConnectionFactory;
    /**
     * 全局配置信息
     */
    private final InvokerProperties invokerProperties;

    public AmqpInvokerSupportAbstractRequest(InvokerProperties invokerProperties) {
        this.invokerProperties = invokerProperties;
        this.defaultConnectionFactory = null;
    }

    public AmqpInvokerSupportAbstractRequest(ConnectionFactory defaultConnectionFactory, InvokerProperties invokerProperties) {
        this.defaultConnectionFactory = defaultConnectionFactory;
        this.invokerProperties = invokerProperties;
        this.multiTenancyConnectionFactory.put("unknown", this.defaultConnectionFactory);
    }

    public Map<String, ConnectionFactory> getMultiTenancyConnectionFactory() {
        return multiTenancyConnectionFactory;
    }

    public ConnectionFactory getDefaultConnectionFactory() {
        return defaultConnectionFactory;
    }

    public InvokerProperties getHttpInvokerProperties() {
        return invokerProperties;
    }

    /**
     * 根据key获取链接
     *
     * @param requestTemplate
     * @return
     */
    protected ConnectionFactory getConnectionFactory(RequestTemplate requestTemplate){

        String amqpConnectionFactoryKey = requestTemplate.getRequestAuthority();

        //根据RemoteAmqpClient中的指定rabbitmq创建连接工厂
        ConnectionFactory connectionFactory = multiTenancyConnectionFactory.get(amqpConnectionFactoryKey);
        if (connectionFactory == null) {

            List<InvokerUrlUtils.NetworkAddress> amqpAddressConfigList = InvokerUrlUtils.resolveNetworkAddressList(requestTemplate.getUri().toString(), ";");
            amqpConnectionFactoryKey = amqpAddressConfigList.stream().map(m -> String.format("%s:%s", m.host(), m.port())).collect(Collectors.joining("-"));

            synchronized (multiTenancyConnectionFactory) {
                connectionFactory = Optional.ofNullable(multiTenancyConnectionFactory.get(amqpConnectionFactoryKey)).orElseGet(() -> {
                    TargetProxy targetProxy = requestTemplate.getTargetProxy();
                    //集群模式
                    if (amqpAddressConfigList.size() > 1) {
                        List<Address> addressList = amqpAddressConfigList.stream().map(m -> new Address(m.host(), m.port())).toList();
                        return AmqpAddress.createConnectionFactory(
                                addressList,
                                targetProxy.getAnnotationAttributeAsString("username"),
                                targetProxy.getAnnotationAttributeAsString("password"),
                                targetProxy.getAnnotationAttributeAsString("virtualHost")
                        );
                    } else {
                        return AmqpAddress.createConnectionFactory(
                                requestTemplate.getUri().getHost(),
                                requestTemplate.getUri().getPort(),
                                targetProxy.getAnnotationAttributeAsString("username"),
                                targetProxy.getAnnotationAttributeAsString("password"),
                                targetProxy.getAnnotationAttributeAsString("virtualHost")
                        );
                    }
                });
                multiTenancyConnectionFactory.put(amqpConnectionFactoryKey, connectionFactory);
            }
        }
        return connectionFactory;
    }

    /**
     * 创建消息监听容器
     *
     * @param connectionFactory
     * @return
     */
    protected AmqpDirectReplyToMessageListenerContainer createListenerContainer(ConnectionFactory connectionFactory) {
        AmqpDirectReplyToMessageListenerContainer messageListenerContainer = new AmqpDirectReplyToMessageListenerContainer(connectionFactory);
        messageListenerContainer.addListener(this);
        messageListenerContainer.addShutdownListener(this);
        messageListenerContainer.afterPropertiesSet();
        messageListenerContainer.start();
        return messageListenerContainer;
    }
}
