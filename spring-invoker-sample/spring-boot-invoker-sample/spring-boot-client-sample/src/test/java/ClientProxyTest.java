import com.boot.invoker.facade.SpringInvokerSampleServiceFacade;
import com.boot.invoker.facade.vo.TestVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.i360day.invoker.DefaultHttpInvokerRequest;
import com.i360day.invoker.HttpInvokerClientMethodInterceptor;
import com.i360day.invoker.InvokerBuilder;
import com.i360day.invoker.codes.decoder.SpringDecode;
import com.i360day.invoker.converter.HttpInvokerHttpMessageConverter;
import com.i360day.invoker.executor.HttpComponentsInvokerRequestExecutor;
import com.i360day.invoker.http.InvokerClient;
import com.i360day.invoker.properties.InvokerProperties;
import com.i360day.invoker.support.ObjectMapperRemoteInvocationFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;

import java.util.Collections;
import java.util.function.Function;

public class ClientProxyTest {

    public static void main(String[] args) {
        TestVo testVo = new TestVo();
        testVo.setUserName("admin");
        testVo.setPassword("123456");

        SpringInvokerSampleServiceFacade facade = InvokerBuilder.create()
                .interfaceClass(SpringInvokerSampleServiceFacade.class)
                .interceptor((Function<InvokerBuilder, MethodInterceptor>) invokerBuilder -> new HttpInvokerClientMethodInterceptor(
                        invokerBuilder.getTargetProxy(),
                        new HttpComponentsInvokerRequestExecutor(new DefaultHttpInvokerRequest(new InvokerClient.Default(new InvokerProperties())), Collections.emptyList()),
                        new ObjectMapperRemoteInvocationFactory(new ObjectMapper()),
                        new SpringDecode(() -> new HttpMessageConverters(new HttpInvokerHttpMessageConverter(new ObjectMapper())))
                ))
                .build();
        String result = facade.testQuery(testVo);
        System.out.println(result);
    }
}
