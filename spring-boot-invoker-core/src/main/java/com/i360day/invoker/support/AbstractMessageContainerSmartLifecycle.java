package com.i360day.invoker.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractMessageContainerSmartLifecycle implements SmartLifecycle, InitializingBean, ApplicationContextAware {
    protected final Logger logger = LoggerFactory.getLogger(AbstractMessageContainerSmartLifecycle.class);
    private boolean running;
    private ApplicationContext applicationContext;
    protected final Lock lifecycleLock = new ReentrantLock(); //NOSONAR
    //    private Executor taskExecutor = (run) -> new Thread(run).start();
    private Executor taskExecutor = ForkJoinPool.commonPool();

    public Executor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * 设置application Context
     *
     * @param applicationContext the ApplicationContext object to be used by this object
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取application context
     *
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public final void start() {
        lifecycleLock.lock();
        try {
            this.running = true;
            doStart();
        } finally {
            lifecycleLock.unlock();
        }
    }

    @Override
    public final void stop() {
        doStop();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 启动
     */
    protected abstract void doStart();

    /**
     * 停止
     */
    protected void doStop() {
        shutdown();
    }

    /**
     * 销毁
     */
    public void shutdown() {
        lifecycleLock.lock();
        try {
            if (!isRunning()) {
                return;
            }
            this.running = false;
        } finally {
            logger.warn("rpc container shutting down");
            lifecycleLock.unlock();
        }
    }
}
