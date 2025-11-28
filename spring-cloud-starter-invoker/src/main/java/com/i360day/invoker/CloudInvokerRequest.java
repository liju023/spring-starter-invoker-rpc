package com.i360day.invoker;

import com.i360day.invoker.http.InvokerClient;
import com.i360day.invoker.http.Request;
import com.i360day.invoker.http.Response;
import com.i360day.invoker.request.InvokerRequest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

import java.io.IOException;
import java.net.URI;

/**
 * Cloud Load Balancer Client
 */
public class CloudInvokerRequest implements InvokerRequest {

    private final InvokerClient delegate;
    private final LoadBalancerClient loadBalancerClient;

    /**
     * http -> LoadBalancerClient
     * @see CloudInvokerClientFactoryBean#CloudInvokerClientFactoryBean
     *
     * @param delegate
     * @param loadBalancerClient
     */
    public CloudInvokerRequest(InvokerClient delegate, LoadBalancerClient loadBalancerClient) {
        this.delegate = delegate;
        this.loadBalancerClient = loadBalancerClient;
    }

    /**
     * 获取注册中心服务地址
     * @param requestTemplate
     * @return
     * @throws IOException
     */
    @Override
    public Response executor(RequestTemplate requestTemplate) throws IOException {
        Request request = requestTemplate.convertRequest();
        final URI originalUri = request.getUri();
        String serviceId = originalUri.getHost();
        ServiceInstance instance = loadBalancerClient.choose(serviceId);
        //优先执行IP直连方式
        if (instance == null) {
            return delegate.execute(request);
        }
        //使用注册中心获取方式
        Request of = Request.of(loadBalancerClient.reconstructURI(instance, originalUri), requestTemplate);
        return delegate.execute(of);
    }
}
