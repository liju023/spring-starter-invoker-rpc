package com.i360day.invoker;

import com.rabbitmq.client.ShutdownSignalException;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import java.util.EventListener;

@FunctionalInterface
public interface AmqpShutdownListener extends EventListener {
    void channelShutdownCompleted(ConnectionFactory defaultConnectionFactory, ShutdownSignalException cause);
}