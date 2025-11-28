import com.boot.invoker.facade.AmqpInvokerSampleServiceFacade;
import com.boot.invoker.facade.hystrix.SpringInvokerSampleServiceHystrix;
import com.boot.invoker.facade.vo.TestVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.i360day.invoker.AmqpInvokerRequest;
import com.i360day.invoker.DefaultHttpInvokerRequest;
import com.i360day.invoker.HttpInvokerClientMethodInterceptor;
import com.i360day.invoker.InvokerBuilder;
import com.i360day.invoker.codes.decoder.SpringDecode;
import com.i360day.invoker.common.InvokerUrlUtils;
import com.i360day.invoker.converter.HttpInvokerHttpMessageConverter;
import com.i360day.invoker.executor.AmqpInvokerRequestExecutor;
import com.i360day.invoker.http.InvokerClient;
import com.i360day.invoker.properties.InvokerProperties;
import com.i360day.invoker.proxy.DefaultTargeterHandler;
import com.i360day.invoker.support.ObjectMapperRemoteInvocationFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;

import java.net.URI;
import java.util.Collections;
import java.util.function.Function;

public class ClientProxyTest {

    public static void main(String[] args) {
        TestVo testVo = new TestVo();
        testVo.setUserName("admin");
        testVo.setPassword("123456");

        AmqpInvokerSampleServiceFacade facade = InvokerBuilder.create()
                .interfaceClass(AmqpInvokerSampleServiceFacade.class)
//                .fallbackFactory(SpringInvokerSampleServiceHystrix.class)
                .interceptor((Function<InvokerBuilder, MethodInterceptor>) (o) -> {
                    return new HttpInvokerClientMethodInterceptor(
                            o.getTargetProxy(),
                            new AmqpInvokerRequestExecutor(new AmqpInvokerRequest(new InvokerProperties())),
                            new ObjectMapperRemoteInvocationFactory(),
                            new SpringDecode(() -> new HttpMessageConverters(new HttpInvokerHttpMessageConverter(new ObjectMapper())))
                    );
                })
                .build();
        String result = facade.testQuery(testVo);
        System.out.println(result);
    }
}
