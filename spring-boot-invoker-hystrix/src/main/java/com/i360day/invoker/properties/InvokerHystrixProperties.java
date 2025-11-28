/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.i360day.invoker.properties;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * @author liju.z
 * @date 2024/3/11 22:29
 */
@ConfigurationProperties(prefix = "spring.invoker.hystrix")
public class InvokerHystrixProperties {
    private boolean enabled;
    private HttpHystrixThreadPoolProperties thread;

    private HttpHystrixCommandProperties command;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public HttpHystrixThreadPoolProperties getThread() {
        return thread;
    }

    public void setThread(HttpHystrixThreadPoolProperties thread) {
        this.thread = thread;
    }

    public HttpHystrixCommandProperties getCommand() {
        return command;
    }

    public void setCommand(HttpHystrixCommandProperties command) {
        this.command = command;
    }

    public HystrixCommandProperties.Setter getSetGet() {
        HystrixCommandProperties.Setter defaultSetter = HystrixCommandProperties.defaultSetter();
        HttpHystrixCommandProperties commandProperties = getCommand();
        if (commandProperties != null) {
            Optional.ofNullable(commandProperties.getCircuitBreakerEnabled()).ifPresent(o -> defaultSetter.withCircuitBreakerEnabled(o));
            Optional.ofNullable(commandProperties.getCircuitBreakerErrorThresholdPercentage()).ifPresent(o -> defaultSetter.withCircuitBreakerErrorThresholdPercentage(o));
            Optional.ofNullable(commandProperties.getCircuitBreakerForceClosed()).ifPresent(o -> defaultSetter.withCircuitBreakerForceClosed(o));
            Optional.ofNullable(commandProperties.getCircuitBreakerForceOpen()).ifPresent(o -> defaultSetter.withCircuitBreakerForceOpen(o));
            Optional.ofNullable(commandProperties.getCircuitBreakerRequestVolumeThreshold()).ifPresent(o -> defaultSetter.withCircuitBreakerRequestVolumeThreshold(o));
            Optional.ofNullable(commandProperties.getCircuitBreakerSleepWindowInMilliseconds()).ifPresent(o -> defaultSetter.withCircuitBreakerSleepWindowInMilliseconds(o));
            Optional.ofNullable(commandProperties.getExecutionIsolationSemaphoreMaxConcurrentRequests()).ifPresent(o -> defaultSetter.withExecutionIsolationSemaphoreMaxConcurrentRequests(o));
            Optional.ofNullable(commandProperties.getExecutionIsolationStrategy()).ifPresent(o -> defaultSetter.withExecutionIsolationStrategy(o));
            Optional.ofNullable(commandProperties.getExecutionIsolationThreadInterruptOnTimeout()).ifPresent(o -> defaultSetter.withExecutionIsolationThreadInterruptOnTimeout(o));
            Optional.ofNullable(commandProperties.getExecutionIsolationThreadInterruptOnFutureCancel()).ifPresent(o -> defaultSetter.withExecutionIsolationThreadInterruptOnFutureCancel(o));
            Optional.ofNullable(commandProperties.getExecutionTimeoutInMilliseconds()).ifPresent(o -> defaultSetter.withExecutionTimeoutInMilliseconds(o));
            Optional.ofNullable(commandProperties.getExecutionTimeoutEnabled()).ifPresent(o -> defaultSetter.withExecutionTimeoutEnabled(o));
            Optional.ofNullable(commandProperties.getFallbackIsolationSemaphoreMaxConcurrentRequests()).ifPresent(o -> defaultSetter.withFallbackIsolationSemaphoreMaxConcurrentRequests(o));
            Optional.ofNullable(commandProperties.getFallbackEnabled()).ifPresent(o -> defaultSetter.withFallbackEnabled(o));
            Optional.ofNullable(commandProperties.getMetricsHealthSnapshotIntervalInMilliseconds()).ifPresent(o -> defaultSetter.withMetricsHealthSnapshotIntervalInMilliseconds(o));
            Optional.ofNullable(commandProperties.getMetricsRollingPercentileBucketSize()).ifPresent(o -> defaultSetter.withMetricsRollingPercentileBucketSize(o));
            Optional.ofNullable(commandProperties.getMetricsRollingPercentileEnabled()).ifPresent(o -> defaultSetter.withMetricsRollingPercentileEnabled(o));
            Optional.ofNullable(commandProperties.getMetricsRollingPercentileWindowInMilliseconds()).ifPresent(o -> defaultSetter.withMetricsRollingPercentileWindowInMilliseconds(o));
            Optional.ofNullable(commandProperties.getMetricsRollingPercentileWindowBuckets()).ifPresent(o -> defaultSetter.withMetricsRollingPercentileWindowBuckets(o));
            Optional.ofNullable(commandProperties.getMetricsRollingStatisticalWindowInMilliseconds()).ifPresent(o -> defaultSetter.withMetricsRollingStatisticalWindowInMilliseconds(o));
            Optional.ofNullable(commandProperties.getMetricsRollingStatisticalWindowBuckets()).ifPresent(o -> defaultSetter.withMetricsRollingStatisticalWindowBuckets(o));
            Optional.ofNullable(commandProperties.getRequestCacheEnabled()).ifPresent(o -> defaultSetter.withRequestCacheEnabled(o));
            Optional.ofNullable(commandProperties.getRequestLogEnabled()).ifPresent(o -> defaultSetter.withRequestLogEnabled(o));
        }
        return defaultSetter;
    }

    public HystrixThreadPoolProperties.Setter getThreadSetter() {
        HystrixThreadPoolProperties.Setter defaultSetter = HystrixThreadPoolProperties.Setter();
        if (getThread() != null) {
            //check
            Assert.isTrue(getThread().getCoreSize() > 0, "coreSize < 0");
            Assert.isTrue(getThread().getMaxQueueSize() > 0, "maxQueueSize < 0");
            Assert.isTrue(getThread().getMaxQueueSize() >= getThread().getCoreSize(), "maxQueueSize < coreSize");
            Assert.isTrue(getThread().getKeepAliveTimeMinutes() > 0, "keepAliveTimeMinutes < 0");

            defaultSetter.withCoreSize(getThread().getCoreSize());
            defaultSetter.withMaximumSize(getThread().getMaximumSize());
            defaultSetter.withMaxQueueSize(getThread().getMaxQueueSize());
            defaultSetter.withKeepAliveTimeMinutes(getThread().getKeepAliveTimeMinutes());
            defaultSetter.withQueueSizeRejectionThreshold(getThread().getQueueSizeRejectionThreshold());
            defaultSetter.withMetricsRollingStatisticalWindowBuckets(getThread().getRollingStatisticalWindowBuckets());
            defaultSetter.withAllowMaximumSizeToDivergeFromCoreSize(getThread().getAllowMaximumSizeToDivergeFromCoreSize());
            defaultSetter.withMetricsRollingStatisticalWindowInMilliseconds(getThread().getRollingStatisticalWindowInMilliseconds());
        }
        return defaultSetter;
    }

    /**
     * HystrixCommandProperties.Setter
     */
    public static class HttpHystrixCommandProperties {
        private Boolean circuitBreakerEnabled = null;
        private Integer circuitBreakerErrorThresholdPercentage = null;
        private Boolean circuitBreakerForceClosed = null;
        private Boolean circuitBreakerForceOpen = null;
        private Integer circuitBreakerRequestVolumeThreshold = null;
        private Integer circuitBreakerSleepWindowInMilliseconds = null;
        private Integer executionIsolationSemaphoreMaxConcurrentRequests = null;
        private HystrixCommandProperties.ExecutionIsolationStrategy executionIsolationStrategy = null;
        private Boolean executionIsolationThreadInterruptOnTimeout = null;
        private Boolean executionIsolationThreadInterruptOnFutureCancel = null;
        private Integer executionTimeoutInMilliseconds = null;
        private Boolean executionTimeoutEnabled = null;
        private Integer fallbackIsolationSemaphoreMaxConcurrentRequests = null;
        private Boolean fallbackEnabled = null;
        private Integer metricsHealthSnapshotIntervalInMilliseconds = null;
        private Integer metricsRollingPercentileBucketSize = null;
        private Boolean metricsRollingPercentileEnabled = null;
        private Integer metricsRollingPercentileWindowInMilliseconds = null;
        private Integer metricsRollingPercentileWindowBuckets = null;
        /* null means it hasn't been overridden */
        private Integer metricsRollingStatisticalWindowInMilliseconds = null;
        private Integer metricsRollingStatisticalWindowBuckets = null;
        private Boolean requestCacheEnabled = null;
        private Boolean requestLogEnabled = null;

        public Boolean getCircuitBreakerEnabled() {
            return circuitBreakerEnabled;
        }

        public void setCircuitBreakerEnabled(Boolean circuitBreakerEnabled) {
            this.circuitBreakerEnabled = circuitBreakerEnabled;
        }

        public Integer getCircuitBreakerErrorThresholdPercentage() {
            return circuitBreakerErrorThresholdPercentage;
        }

        public void setCircuitBreakerErrorThresholdPercentage(Integer circuitBreakerErrorThresholdPercentage) {
            this.circuitBreakerErrorThresholdPercentage = circuitBreakerErrorThresholdPercentage;
        }

        public Boolean getCircuitBreakerForceClosed() {
            return circuitBreakerForceClosed;
        }

        public void setCircuitBreakerForceClosed(Boolean circuitBreakerForceClosed) {
            this.circuitBreakerForceClosed = circuitBreakerForceClosed;
        }

        public Boolean getCircuitBreakerForceOpen() {
            return circuitBreakerForceOpen;
        }

        public void setCircuitBreakerForceOpen(Boolean circuitBreakerForceOpen) {
            this.circuitBreakerForceOpen = circuitBreakerForceOpen;
        }

        public Integer getCircuitBreakerRequestVolumeThreshold() {
            return circuitBreakerRequestVolumeThreshold;
        }

        public void setCircuitBreakerRequestVolumeThreshold(Integer circuitBreakerRequestVolumeThreshold) {
            this.circuitBreakerRequestVolumeThreshold = circuitBreakerRequestVolumeThreshold;
        }

        public Integer getCircuitBreakerSleepWindowInMilliseconds() {
            return circuitBreakerSleepWindowInMilliseconds;
        }

        public void setCircuitBreakerSleepWindowInMilliseconds(Integer circuitBreakerSleepWindowInMilliseconds) {
            this.circuitBreakerSleepWindowInMilliseconds = circuitBreakerSleepWindowInMilliseconds;
        }

        public Integer getExecutionIsolationSemaphoreMaxConcurrentRequests() {
            return executionIsolationSemaphoreMaxConcurrentRequests;
        }

        public void setExecutionIsolationSemaphoreMaxConcurrentRequests(Integer executionIsolationSemaphoreMaxConcurrentRequests) {
            this.executionIsolationSemaphoreMaxConcurrentRequests = executionIsolationSemaphoreMaxConcurrentRequests;
        }

        public HystrixCommandProperties.ExecutionIsolationStrategy getExecutionIsolationStrategy() {
            return executionIsolationStrategy;
        }

        public void setExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy executionIsolationStrategy) {
            this.executionIsolationStrategy = executionIsolationStrategy;
        }

        public Boolean getExecutionIsolationThreadInterruptOnTimeout() {
            return executionIsolationThreadInterruptOnTimeout;
        }

        public void setExecutionIsolationThreadInterruptOnTimeout(Boolean executionIsolationThreadInterruptOnTimeout) {
            this.executionIsolationThreadInterruptOnTimeout = executionIsolationThreadInterruptOnTimeout;
        }

        public Boolean getExecutionIsolationThreadInterruptOnFutureCancel() {
            return executionIsolationThreadInterruptOnFutureCancel;
        }

        public void setExecutionIsolationThreadInterruptOnFutureCancel(Boolean executionIsolationThreadInterruptOnFutureCancel) {
            this.executionIsolationThreadInterruptOnFutureCancel = executionIsolationThreadInterruptOnFutureCancel;
        }

        public Integer getExecutionTimeoutInMilliseconds() {
            return executionTimeoutInMilliseconds;
        }

        public void setExecutionTimeoutInMilliseconds(Integer executionTimeoutInMilliseconds) {
            this.executionTimeoutInMilliseconds = executionTimeoutInMilliseconds;
        }

        public Boolean getExecutionTimeoutEnabled() {
            return executionTimeoutEnabled;
        }

        public void setExecutionTimeoutEnabled(Boolean executionTimeoutEnabled) {
            this.executionTimeoutEnabled = executionTimeoutEnabled;
        }

        public Integer getFallbackIsolationSemaphoreMaxConcurrentRequests() {
            return fallbackIsolationSemaphoreMaxConcurrentRequests;
        }

        public void setFallbackIsolationSemaphoreMaxConcurrentRequests(Integer fallbackIsolationSemaphoreMaxConcurrentRequests) {
            this.fallbackIsolationSemaphoreMaxConcurrentRequests = fallbackIsolationSemaphoreMaxConcurrentRequests;
        }

        public Boolean getFallbackEnabled() {
            return fallbackEnabled;
        }

        public void setFallbackEnabled(Boolean fallbackEnabled) {
            this.fallbackEnabled = fallbackEnabled;
        }

        public Integer getMetricsHealthSnapshotIntervalInMilliseconds() {
            return metricsHealthSnapshotIntervalInMilliseconds;
        }

        public void setMetricsHealthSnapshotIntervalInMilliseconds(Integer metricsHealthSnapshotIntervalInMilliseconds) {
            this.metricsHealthSnapshotIntervalInMilliseconds = metricsHealthSnapshotIntervalInMilliseconds;
        }

        public Integer getMetricsRollingPercentileBucketSize() {
            return metricsRollingPercentileBucketSize;
        }

        public void setMetricsRollingPercentileBucketSize(Integer metricsRollingPercentileBucketSize) {
            this.metricsRollingPercentileBucketSize = metricsRollingPercentileBucketSize;
        }

        public Boolean getMetricsRollingPercentileEnabled() {
            return metricsRollingPercentileEnabled;
        }

        public void setMetricsRollingPercentileEnabled(Boolean metricsRollingPercentileEnabled) {
            this.metricsRollingPercentileEnabled = metricsRollingPercentileEnabled;
        }

        public Integer getMetricsRollingPercentileWindowInMilliseconds() {
            return metricsRollingPercentileWindowInMilliseconds;
        }

        public void setMetricsRollingPercentileWindowInMilliseconds(Integer metricsRollingPercentileWindowInMilliseconds) {
            this.metricsRollingPercentileWindowInMilliseconds = metricsRollingPercentileWindowInMilliseconds;
        }

        public Integer getMetricsRollingPercentileWindowBuckets() {
            return metricsRollingPercentileWindowBuckets;
        }

        public void setMetricsRollingPercentileWindowBuckets(Integer metricsRollingPercentileWindowBuckets) {
            this.metricsRollingPercentileWindowBuckets = metricsRollingPercentileWindowBuckets;
        }

        public Integer getMetricsRollingStatisticalWindowInMilliseconds() {
            return metricsRollingStatisticalWindowInMilliseconds;
        }

        public void setMetricsRollingStatisticalWindowInMilliseconds(Integer metricsRollingStatisticalWindowInMilliseconds) {
            this.metricsRollingStatisticalWindowInMilliseconds = metricsRollingStatisticalWindowInMilliseconds;
        }

        public Integer getMetricsRollingStatisticalWindowBuckets() {
            return metricsRollingStatisticalWindowBuckets;
        }

        public void setMetricsRollingStatisticalWindowBuckets(Integer metricsRollingStatisticalWindowBuckets) {
            this.metricsRollingStatisticalWindowBuckets = metricsRollingStatisticalWindowBuckets;
        }

        public Boolean getRequestCacheEnabled() {
            return requestCacheEnabled;
        }

        public void setRequestCacheEnabled(Boolean requestCacheEnabled) {
            this.requestCacheEnabled = requestCacheEnabled;
        }

        public Boolean getRequestLogEnabled() {
            return requestLogEnabled;
        }

        public void setRequestLogEnabled(Boolean requestLogEnabled) {
            this.requestLogEnabled = requestLogEnabled;
        }
    }

    /**
     * HystrixThreadPoolProperties.Setter
     */
    public static class HttpHystrixThreadPoolProperties {
        private int coreSize = 10;
        private int maximumSize = 10;
        private int keepAliveTimeMinutes = 1;
        private int maxQueueSize = -1;
        private int queueSizeRejectionThreshold = 5;
        private Boolean allowMaximumSizeToDivergeFromCoreSize = false;
        private int rollingStatisticalWindowInMilliseconds = 10000;
        private int rollingStatisticalWindowBuckets = 10;

        public int getCoreSize() {
            return coreSize;
        }

        public void setCoreSize(int coreSize) {
            this.coreSize = coreSize;
        }

        public int getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(int maximumSize) {
            this.maximumSize = maximumSize;
        }

        public int getKeepAliveTimeMinutes() {
            return keepAliveTimeMinutes;
        }

        public void setKeepAliveTimeMinutes(int keepAliveTimeMinutes) {
            this.keepAliveTimeMinutes = keepAliveTimeMinutes;
        }

        public int getMaxQueueSize() {
            return maxQueueSize;
        }

        public void setMaxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
        }

        public int getQueueSizeRejectionThreshold() {
            return queueSizeRejectionThreshold;
        }

        public void setQueueSizeRejectionThreshold(int queueSizeRejectionThreshold) {
            this.queueSizeRejectionThreshold = queueSizeRejectionThreshold;
        }

        public Boolean getAllowMaximumSizeToDivergeFromCoreSize() {
            return allowMaximumSizeToDivergeFromCoreSize;
        }

        public void setAllowMaximumSizeToDivergeFromCoreSize(Boolean allowMaximumSizeToDivergeFromCoreSize) {
            this.allowMaximumSizeToDivergeFromCoreSize = allowMaximumSizeToDivergeFromCoreSize;
        }

        public int getRollingStatisticalWindowInMilliseconds() {
            return rollingStatisticalWindowInMilliseconds;
        }

        public void setRollingStatisticalWindowInMilliseconds(int rollingStatisticalWindowInMilliseconds) {
            this.rollingStatisticalWindowInMilliseconds = rollingStatisticalWindowInMilliseconds;
        }

        public int getRollingStatisticalWindowBuckets() {
            return rollingStatisticalWindowBuckets;
        }

        public void setRollingStatisticalWindowBuckets(int rollingStatisticalWindowBuckets) {
            this.rollingStatisticalWindowBuckets = rollingStatisticalWindowBuckets;
        }
    }
}
