#### 介绍
<h1><b>spring boot > 3.0.0 + 版本、 jdk 17 + </b></h1>
&nbsp;&nbsp;&nbsp;&nbsp;一个简单轻量的rpc框架，可以不使用任何注册中心或使用注册中心两种方式。<br/>
&nbsp;&nbsp;&nbsp;&nbsp;几年前在一次项目开发中偶然突发奇想为啥不用service与service之间通信呢岂不是更加舒适，于是基于这种模式在接下来的时间经过不断的摸索和推论最终以spring invoker为切入点实现了service与service
之间像使用本地上下文这样直接IOC后调用。然而在后来的开发中又遇多种业务的限制、演变出多种模式的rpc如。在某些特定场景下也可以使用webSocket、redis、rabbitmq单向网络实现rpc。后期逐步会在加入netty模式主要想达到协议的转发
以及rpc可视化的控制台目前已再运量中。<br/> 
&nbsp;&nbsp;&nbsp;&nbsp;目前invoker rpc序列化支持jdk、json两种方式，并基于spring cloud loadbalancer模块，实现nacos、zookeeper、eureka、consul等注册中心模式。同时也支持项目中没有注册中心场景下的项目，
该invoker rpc类同于openfeign等一个远程调用工具。<br/> 
&nbsp;&nbsp;&nbsp;&nbsp;直接使用@EnableRemoteDiscoveryClient自动发现或注册客户端与服务端，其次在service类的接口中定义（@RemoteClient、@RemoteRedisClient、@RemoteWebSocketClient, @RemoteCloudClient、@RemoteAmqpClient）注解
即可，老项目也易改造、能做到零侵入项目快速满足项目需求。<br/> 

仓库地址：https://gitee.com/liju023/spring-cloud-invoker-parent

#### 支持模式</br>
1）支持eureka注册中心 <br/> 
2）支持nacos注册中心 <br/>
3）支持consul注册中心  <br/>
4）支持zookeeper注册中心  <br/>
5）支持IP直连方式 <br/>
6）支持redis直连、集群方式 <br/>
7）支持rabbitmq直连、集群方式 <br/>
8）支持webSocket直连方式 <br/>

#### 更新说明
<p>1、  3.0.2 开启spring boot 3.0 + 版本、jdk 17 支持</p>

### application.yml 配置

```
spring:
  invoker:
    #默认使用json方式序列化
    serializable: json、jdk 
    #request 配置
    request:
      #请求客户端
      client: httpClient || okHttp
      #最大链接数
      maxConnections: 100 
      #最大空闲链接，单位毫秒
      timeToLive: 1000 * 30
      #请求超时
      connectionRequestTimeout: 15000 
      #连接超时
      connectTimeout: 20000   
      #读取超时
      readTimeout: 15000  
    #全局客户端配置
    global-client:
      #回调
      fallback: com.xxx.TestFallback
      #回调工厂
      fallbackFactory: com.xxx.FallbackFactory
      #客户端rpc请求账号
      client-user: invoker-user
      #客户端rpc请求密码
      client-password: invoker-password
    #全局服务端配置
    global-server:
      #服务端验证账号
      server-user: invoker-user
      #服务端验证密码
      server-password: invoker-password
    webSocket: 
      #webSocket模式下，握手地址
      endpoint: /websocket-invoker
      #socket账号
      server-user-name: invoker-username
      #socket密码
      server-password: 123456
    #hystrix配置
    hystrix:
        #启动熔断降级处理，默认true
      enabled: true 
      command:
        #断路器启用
        circuitBreakerEnabled: true
        #断路器错误阈值百分比
        circuitBreakerErrorThresholdPercentage: 90
        #断路器强制关闭
        circuitBreakerForceClosed: false
        #断路器强行打开
        circuitBreakerForceOpen: true
        #断路器请求容量阈值
        circuitBreakerRequestVolumeThreshold: 100
        #断路器睡眠窗口（毫秒）
        circuitBreakerSleepWindowInMilliseconds: 1000
        #执行隔离信号量最大并发请求
        executionIsolationSemaphoreMaxConcurrentRequests: 1000
        private HystrixCommandProperties.ExecutionIsolationStrategy executionIsolationStrategy = null;
        #超时时执行隔离线程中断
        executionIsolationThreadInterruptOnTimeout: true
        #执行隔离线程中断未来取消
        executionIsolationThreadInterruptOnFutureCancel: false
        #执行时间（毫秒）
        executionTimeoutInMilliseconds: 3000
        #执行超时启用
        executionTimeoutEnabled: true
        #回退隔离信号量最大并发请求
        fallbackIsolationSemaphoreMaxConcurrentRequests: 100
        #回调启用
        fallbackEnabled: true
        #度量健康快照间隔（毫秒）
        metricsHealthSnapshotIntervalInMilliseconds: 3000
        #度量滚动百分比桶大小
        metricsRollingPercentileBucketSize: 100
        #度量滚动百分比已启用
        metricsRollingPercentileEnabled: true
        #度量滚动百分比窗口（单位：毫秒）
        metricsRollingPercentileWindowInMilliseconds: 100
        #度量滚动百分比窗口桶
        metricsRollingPercentileWindowBuckets: 1
        #度量滚动统计窗口（单位：毫秒）
        metricsRollingStatisticalWindowInMilliseconds: 3000
        #度量滚动统计窗口Buckets
        metricsRollingStatisticalWindowBuckets: 1
        #请求缓存启动
        requestCacheEnabled: true
        #请求日志启动
        requestLogEnabled: true
      thread: 
        coreSize = 10;
        maximumSize = 10;
        keepAliveTimeMinutes = 1;
        maxQueueSize = -1;
        queueSizeRejectionThreshold = 5;
        allowMaximumSizeToDivergeFromCoreSize = false;
        rollingStatisticalWindowInMilliseconds = 10000;
        rollingStatisticalWindowBuckets = 10;
```

### maven

| 依赖                                      | 版本    | 描述                 |
|-----------------------------------------|-------|--------------------|
| spring-boot-invoker-annotations         | 3.0.4 | invoker注解包         |
| spring-boot-invoker-core                | 3.0.4 | invoker核心包         |
| spring-boot-invoker-hystrix             | 3.0.4 | invoker熔断          |
| spring-boot-starter-invoker             | 3.0.4 | spring boot使用      |
| spring-boot-starter-redis-invoker       | 3.0.4 | spring redis使用     |
| spring-boot-starter-amqp-invoker        | 3.0.4 | spring rabbitmq使用  |
| spring-boot-starter-websocket-invoker   | 3.0.4 | spring websocket使用 |
| spring-cloud-starter-invoker            | 3.0.4 | spring微服务使用        |
| spring-invoker-dependencies             | 3.0.4 | 版本依赖               |

1. pom.xml 添加如下代码

```
<dependency>
    <groupId>com.i360day</groupId>
    <artifactId>spring-invoker-dependencies</artifactId>
    <version>${project.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
            
```

### 使用说明
#### 1、requestTemplate 拦截器
```java
public interface InvokerRequestInterceptor {
    /**
     * 对requestTemplate进行设置
     *
     * @param requestTemplate
     */
    void apply(RequestTemplate requestTemplate);
}
```

#### 2、目标代理类可重写
```java
public interface Targeter {

    <T> MethodInterceptor target(Map<Method, MethodInterceptor> dispatchMap, FallbackFactory fallbackFactory, TargetProxy<T> target);

}
```
#### 3、解码和加码器
框架使用了jdk、json两种模式进行数据的加解码；原理是把rpc的参数进行有序、序列化后设置到RemoteInvocation类中，再使用jdk把RemoteInvocation数据包序列化传入请求中，服务端接收到后再对RemoteInvocation解码操作。<br/>

```java
public interface Decoder {

    /**
     * 解析response响应结果
     * @param response
     * @param type
     * @return RemoteInvocationResult
     * @throws IOException
     */
    RemoteInvocationResult decode(Response response, Type type) throws IOException, ClassNotFoundException;
}

public interface Encoder {
    /**
     * 对RemoteInvocationResult对象编码
     * @param outputStream
     * @param responseType
     * @param result
     * @throws IOException
     */
    void encoder(OutputStream outputStream, Type responseType, RemoteInvocationResult result) throws IOException;
}
```
#### 4、客户端接口定义
1、rpc接口可支持类泛型，方法泛型、其中返回泛型需要结合@RemoteResponseBody进行使用，@RemoteRequestParam可对参数指定序列化方式</br>
2、rpc接口可继承一个有@RemoteModule标记的类，其目的将公共属性单独配置（如：address、clientUser、clientPassword）</br>
3、rpc接口上的@RemoteClient标记，可自定义decoder，encoder加解码、fallbackFactory降级处理、clientProxyClass客户端代理类、serverProxyClass服务端代理类、group分组、version版本</br>
4、rpc接口方法上可使用@RemoteDisable标记未禁止调用
5、如服务端设置了账号密码，客户端应对其设置@RemoteModule(clientUser="invoker-user", clientPassword="invoker-password")
```
spring:
  invoker:
    #默认使用json方式序列化
    serializable: json、jdk 
    #request 配置
    request:
      #请求客户端
      client: httpClient || okHttp
      #最大链接数
      maxConnections: 100 
      #最大空闲链接，单位毫秒
      timeToLive: 1000 * 30
      #请求超时
      connectionRequestTimeout: 15000 
      #连接超时
      connectTimeout: 20000   
      #读取超时
      readTimeout: 15000  
    #全局客户端配置
    global-client:
      #回调
      fallback: com.xxx.TestFallback
      #回调工厂
      fallbackFactory: com.xxx.FallbackFactory
      #客户端rpc请求账号
      client-user: invoker-user
      #客户端rpc请求密码
      client-password: invoker-password
```

```java
@RemoteClient(decoder = SpringDecode.class, encoder = SpringEncoder.class, fallbackFactory = SpringInvokerSampleServiceHystrix.class)
public interface SpringInvokerSampleServiceFacade extends SpringInvokerSampleServiceModule {

    /***
     * 方法禁止调用定义
     * @param testVo
     * @return
     */
    @RemoteDisable
    String testQuery(TestVo testVo);

    /***
     * 多泛型的定义
     * @param testVo
     * @param params
     * @param testVoSet
     * @param clazz
     * @param testVoMap
     * @param httpStatus
     * @return
     * @param <T>
     */
    <T> List<TestVo> list(List<List<List<TestVo>>> testVo, Map<String, Map<String, Map<Object, Object>>> params, Set<TestVo> testVoSet, Class<TestVo> clazz, Map<String, TestVo> testVoMap, @RemoteRequestParam(serialize = HttpStatus.class) HttpStatusCode httpStatus);

    /**
     * 异步调用
     * @param strs
     * @param testVos
     * @param number
     * @param numbers
     */
    @Async
    void testArrays(String[] strs, TestVo[] testVos, int number, int[] numbers);

    /**
     * 返回空值或任意对象
     * @param obj
     * @return
     */
    Object testNull(Object obj);

    /**
     * 无参使用方式
     * @return
     */
    TestVo testVoid();

    /**
     * 对数组的序列化
     * @param bytes1
     * @param bytes2
     * @return
     */
    TestVo testByte(byte[] bytes1, Byte[] bytes2);

    /**
     * 多模式定义
     * @param clazz             定义有泛型的class
     * @param testVo            定义类
     * @param set               定义有泛型的set
     * @param a                 定义有泛型<?>的list
     * @param list              定义返回泛型的list
     * @param param             定义返回泛型和<?>泛型的map
     * @param httpStatusCode    定义枚举指定@RemoteRequestParam序列化
     * @param httpStatus        定义有泛型的枚举
     * @return <T,O,E>          结合@RemoteResponseBody返回指定泛型
     */
    @RemoteResponseBody(deserialize = TestVo.class)
    <T, O, E> T testSerialize(Class<? extends HttpInvokerEvent> clazz, TestVo testVo, Set<? extends Object> set, List<?>[] a, List<O> list, Map<E, ?> param, @RemoteRequestParam(serialize = HttpStatus.class) HttpStatusCode httpStatusCode, Enum<? extends HttpStatus> httpStatus);
}
```

#### 5、服务端接口定义
1、当接口已在上下文中，会使用上下文的service。不会在对接口进行代理
2、可定义请求时必须携带账号密码进行请求，并content-type必须是application/x-rpc-http-invoker
3、可自定义RemoteInvocationExecutor执行器类，自定义处理请求
```
spring:
  invoker:
    #默认使用json方式序列化
    serializable: json、jdk 
    #全局服务端配置
    global-server:
      #服务端验证账号
      server-user: invoker-user
      #服务端验证密码
      server-password: invoker-password
```

```java
public interface RemoteInvocationExecutor {

    /**
     * 执行器
     * @param invocation
     * @param targetObject
     * @param targetInterface
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
	Object invoke(RemoteInvocation invocation, Object targetObject, Class<?> targetInterface) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException;

}
```

#### 1、使用main方法调用远程rpc
```java

public class RemoteMainTest{

    /**
     * http客户端 请求
     */
    public void rabbitmqTest(){
        AmqpInvokerSampleServiceFacade facade = InvokerBuilder.create()
                //接口class
                .interfaceClass(SpringInvokerSampleServiceFacade.class)
                //降级处理
                .fallbackFactory(SpringInvokerSampleServiceHystrix.class)
                //
                .interceptor((Function<InvokerBuilder, MethodInterceptor>) (o) -> {
                    return new HttpInvokerClientMethodInterceptor(
                            o.getTargetProxy(),
                            new HttpComponentsInvokerRequestExecutor(new AmqpInvokerRequest(new InvokerProperties())),
                            new ObjectMapperRemoteInvocationFactory(),
                            new SpringDecode(() -> new HttpMessageConverters(new HttpInvokerHttpMessageConverter(new ObjectMapper())))
                    );
                })
                .build();
        String result = facade.testQuery(testVo);
    }
    
    /**
     * mq客户端 请求
     */
    public void rabbitmqTest(){
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
    }
} 
```

#### 1、spring-boot-starter-invoker	方式
##### 启动类添加@EnableRemoteDiscoveryClient注解<br/>
```java
@SpringBootApplication
@EnableRemoteDiscoveryClient
public class TestBootstrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(TestBootstrap.class);
    }
}
```

##### 2、 提供者 service <br/>
service类需要实现一个接口<br/>
而这个接口如果在消费端就会自动注册成一个远程客户端，如果在本工程就会被代理成HttpRequestHandler
```java
//不会对当前service做任何处理，唯一需要实现一个接口
@Service
public class TestService implements TestServiceFacade {
   public String test(){
	return "ok";
   }
}

//可指定编码和解码器：decoder = SpringDecode.class, encoder = SpringEncoder.class
//当使用了RemoteModule模块，可忽略(address="http://127.0.0.1:9092/invoker-service")，接口调用失败后使用fallbackFactory方式降级处理
@RemoteClient(address="http://127.0.0.1:9092/invoker-service", fallbackFactory = TestServiceHystrix.class)
public interface TestServiceFacade extends TestModule{
    String test();
}

//可使用模块类来区分配置或者使用配置文件， 注：address是完整的地址、IP + 端口 + /项目名称， 没有则忽略（/invoker-service）
@RemoteModule(address = "${spring.invoker.xxx.address:http://127.0.0.1:9092/invoker-service}")
public interface TestModule{

}
```
##### 3、消费者 controller <br/>
```java
@RestController
@RequestMapping("/test")
public class TestController{
    
    @Autowired
    private TestServiceFacade testServiceFacade;
    
    @RequestMapping("/query")
    public Object query(){
        //调用后以rpc方式进行请求
        return testServiceFacade.test();
    }
    
}
```
#### 2、spring-cloud-starter-invoker	 方式
##### 启动类添加@EnableRemoteDiscoveryClient注解<br/>
```java
@SpringBootApplication
@EnableRemoteDiscoveryClient
public class TestBootstrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(TestBootstrap.class);
    }
}
```

##### 2、 提供者 service <br/>
service类需要实现一个接口<br/>
而这个接口如果在消费端就会自动注册成一个远程客户端，如果在本工程就会被代理成HttpRequestHandler
###### 2.1 默认会支持IP直连方式
###### 2.2 使用注册（nacos、zookeeper、eureka、consul）中心调用
```java
//不会对当前service做任何处理，唯一需要实现一个接口
@Service
public class CloudTestService implements CloudTestServiceFacade {
   public String test(){
	return "ok";
   }
}

//当使用了RemoteModule模块，可忽略(name="invoker-service")，接口调用失败后使用fallbackFactory方式降级处理
@RemoteCloudClient(address="https://${spring.application.name:invoker-service}/invoker-server", fallbackFactory = CloudTestServiceHystrix.class)
public interface CloudTestServiceFacade extends TestModule{
    String test();
}

//可使用模块类来区分配置或者使用配置文件， 注：address是完整的地址、IP + 端口 + /项目名称， 没有则忽略（/invoker-service）
@RemoteModule(address = "https://${spring.application.name:invoker-service}/invoker-server")
public interface CloudTestModule{

}
```
##### 3、 消费者 controller <br/>
```java
@RestController
@RequestMapping("/test")
public class TestController{
    
    @Autowired
    private TestServiceFacade testServiceFacade;
    
    @Autowired
    private CloudTestServiceFacade cloudTestServiceFacade;
    
    //http IP直连调用
    @RequestMapping("/query1")
    public Object query(){
        return testServiceFacade.test();
    }
    
    //nacos、zookeeper、eureka、consul注册中心调用
    @RequestMapping("/query2")
    public Object query(){
        return cloudTestServiceFacade.test();
    }
    
}
```


#### 3、spring-boot-starter-redis-invoker		 方式
##### 启动类添加@EnableRemoteDiscoveryClient注解<br/>
```java
@SpringBootApplication
@EnableRemoteDiscoveryClient
public class TestBootstrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(TestBootstrap.class);
    }
}
```

##### 2、 提供者 service <br/>
service类需要实现一个接口<br/>
而这个接口如果在消费端就会自动注册成一个远程客户端，如果在本工程就会被代理成HttpRequestHandler<br/>
###### 2.1 默认会支持IP直连方式
###### 2.2 使用redis方式访问
```java
//不会对当前service做任何处理，唯一需要实现一个接口
@Service
public class RedisTestService implements RedisTestServiceFacade {
    public String test(){
        return "ok";
    }
}

//当使用了RemoteModule模块，可忽略(address="redis://127.0.0.1:6379/invoker-service")，接口调用失败后使用fallbackFactory方式降级处理
@RemoteRedisClient(address="redis://127.0.0.1:6379/invoker-service", fallbackFactory = TestServiceHystrix.class)
public interface RedisTestServiceFacade extends RedisTestModule{
    String test();
}

//可使用模块类来区分配置或者使用配置文件， 注：address是完整的地址、IP + 端口 + /项目名称， 没有则忽略（/invoker-service）
@RemoteModule(address="redis://127.0.0.1:6379/invoker-service")
public interface RedisTestModule{

}
```

##### 3、 消费者 controller <br/>
```java
@RestController
@RequestMapping("/test")
public class TestController{

    @Autowired
    private TestServiceFacade testServiceFacade;

    @Autowired
    private RedisTestServiceFacade redisTestServiceFacade;

    //使用http方式调用接口
    @RequestMapping("/query1")
    public Object query(){
        return testServiceFacade.test();
    }
    
    //使用redis方式调用接口
    @RequestMapping("/query2")
    public Object query(){
        return redisTestServiceFacade.test();
    }

}
```



#### 4、spring-boot-starter-amqp-invoker			 方式
##### 启动类添加@EnableRemoteDiscoveryClient注解<br/>
```java
@SpringBootApplication
@EnableRemoteDiscoveryClient
public class TestBootstrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(TestBootstrap.class);
    }
}
```

##### 2、 提供者 service <br/>
service类需要实现一个接口<br/>
而这个接口如果在消费端就会自动注册成一个远程客户端，如果在本工程就会被代理成HttpRequestHandler<br/>
###### 2.1 默认会支持IP直连方式
###### 2.2 使用rabbitmq方式访问
```java
//不会对当前service做任何处理，唯一需要实现一个接口
@Service
public class AmqpTestService implements AmqpTestServiceFacade {
    public String test(){
        return "ok";
    }
}

//1、当是集群方式则以英文逗号分割（amqp://127.0.0.1:5672,amqp://127.0.0.1:5672）
//2、如果是多个地址，则在列表中随机抽取访问
//当使用了RemoteModule模块，可忽略(address="amqp://127.0.0.1:5672")，接口调用失败后使用fallbackFactory方式降级处理
@RemoteAmqpClient(address="amqp://127.0.0.1:5672", fallbackFactory = AmqpServiceHystrix.class)
public interface AmqpTestServiceFacade extends AmqpTestModule{
    String test();
}

//可使用模块类来区分配置或者使用配置文件， 注：如果没有项目名（contextPath）则为空或不填写
@RemoteModule(address="amqp://127.0.0.1:5672")
public interface AmqpTestModule{

}
```

##### 3、 消费者 controller <br/>
```java
@RestController
@RequestMapping("/test")
public class TestController{

    @Autowired
    private TestServiceFacade testServiceFacade;

    @Autowired
    private AmqpTestServiceFacade amqpTestServiceFacade;

    //使用http方式调用接口
    @RequestMapping("/query1")
    public Object query(){
        return testServiceFacade.test();
    }
    
    //使用rabbitmq方式调用接口
    @RequestMapping("/query2")
    public Object query(){
        return amqpTestServiceFacade.test();
    }

}
```


#### 5、spring-boot-starter-websocket-invoker				 方式
##### 启动类添加@EnableRemoteDiscoveryClient注解<br/>
```java
@SpringBootApplication
@EnableRemoteDiscoveryClient
public class TestBootstrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(TestBootstrap.class);
    }
}
```

##### 2、 提供者 service <br/>
service类需要实现一个接口<br/>
而这个接口如果在消费端就会自动注册成一个远程客户端，如果在本工程就会被代理成HttpRequestHandler<br/>
###### 2.1 默认会支持IP直连方式
###### 2.2 使用webSocket方式访问
```java
//不会对当前service做任何处理，唯一需要实现一个接口
@Service
public class WebSocketTestService implements WebSocketTestServiceFacade {
    public String test(){
        return "ok";
    }
}

//当使用了RemoteModule模块，可忽略(address="ws://127.0.0.1:9092/invoker-service")，接口调用失败后使用fallbackFactory方式降级处理
@RemoteWebSocketClient(address="ws://127.0.0.1:9092/invoker-service", fallbackFactory = AmqpServiceHystrix.class)
public interface WebSocketTestServiceFacade extends AmqpTestModule{
    String test();
}

//可使用模块类来区分配置或者使用配置文件， 注：如果没有项目名（contextPath）则为空或不填写
@RemoteModule(address="ws://127.0.0.1:9092/invoker-service")
public interface WebSocketTestModule{

}
```

##### 3、 消费者 controller <br/>
```java
@RestController
@RequestMapping("/test")
public class TestController{

    @Autowired
    private TestServiceFacade testServiceFacade;

    @Autowired
    private WebSocketTestServiceFacade webSocketTestServiceFacade;

    //使用http方式调用接口
    @RequestMapping("/query1")
    public Object query(){
        return testServiceFacade.test();
    }
    
    //使用webSocket方式调用接口
    @RequestMapping("/query2")
    public Object query(){
        return webSocketTestServiceFacade.test();
    }

}
```


#### 6、security oauth2 服务调用验证

```java
//当开启了security后，可使用RemoteIgnoreService忽略该service无权限访问
@RemoteIgnoreService
public class TestService{
    //...
}

//加入请求头
@Component
public class RequestInterceptor implements HttpInvokerRequestInterceptor {
	@Override
	public void apply(RequestTemplate requestTemplate) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if ((authentication instanceof AbstractAuthenticationToken)) {
			AbstractAuthenticationToken aat = (AbstractAuthenticationToken)authentication;
			if ((aat.getDetails() instanceof OAuth2AuthenticationDetails)) {
				OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails)aat.getDetails();
                requestTemplate.addHeaders("Authorization", new String(String.format("%s %s", new Object[] { details.getTokenType(), details.getTokenValue() })));
			}
		}
	}
}
```

#### 参与贡献


