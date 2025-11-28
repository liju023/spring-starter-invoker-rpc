package com.i360day.invoker.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author: 胡.青牛
 * @date: 2019/5/13   11:36
 **/
public abstract class InvokerConstant {
    /**
     * 分割线
     */
    public final static String SEPARATOR = "/";
    /**
     * 定义远程调用固定值
     */
    public final static String FIXED_URL = SEPARATOR + "remote/server" + SEPARATOR;
    /**
     * 服务跟踪拦截器
     */
    public final static String REMOTE_INVOCATION_TRACE_INTERCEPTOR = "remoteInvocationTraceInterceptor";
    /**
     * 服务调用执行器
     */
    public final static String REMOTE_INVOCATION_EXECUTOR = "remoteInvocationExecutor";
    /**
     * 协议值
     */
    public static final String ACCEPT_RPC_HTTP_INVOKER = "application/x-rpc-http-invoker";
    /**
     * 协议头
     */
    public static final String ACCEPT_RPC_HTTP_INVOKER_KEY = "Accept";
    /**
     * 认证key
     */
    public static final String AUTHORIZATION = "Authorization-Invoker";
    /**
     * 时间戳
     */
    public static final String ACCEPT_TIMESTAMP = "Accept-Timestamp";
    /**
     * 判断当前请求header中是否包含该字段
     */
    public final static String HTTP_INVOKER_AUTHORIZATION_KEY = "Accept-HttpInvoker-Authorization";
    /**
     * 请求头中对应HTTP_INVOKER_AUTHORIZATION_KEY字段的值
     */
    public final static String HTTP_INVOKER_AUTHORIZATION_VALUE = "true";
    /**
     *
     **/
    public final static String INVOKER_SECURITY_FILTER_CHAIN = "INVOKER_SECURITY_FILTER_CHAIN";

    /**
     * 默认object mapper
     */
    public final static ObjectMapper objectMapper;

    /**
     * static
     */
    static {
        objectMapper = new ObjectMapper();
        //允许单引号来包住属性名称和字符串值
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //忽略多余的json字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //允许JSON空字符串转换为POJO对象为null
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        //允许JSON字符串包含非引号控制字符（值小于32的ASCII字符，包含制表符和换行符）。 如果该属性关闭，则如果遇到这些字符，则会抛出异常
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        // 设置默认时间格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss"));
    }

    /**
     * @param value
     * @Description: 验证 HTTP_INVOKER_AUTHORIZATION_KEY字段值是否一致
     * @author: 胡.青牛
     * @Date: 2019/6/11 0011 17:23
     * @return:
     **/
    public static boolean verify(String value) {
        return ObjectUtils.isEquals(value, HTTP_INVOKER_AUTHORIZATION_VALUE);
    }

    /**
     * 签名
     *
     * @param returnType
     * @param methodName
     * @param args
     * @return java.lang.String
     */
    public static String sign(String methodName, Type returnType, Object[] args) {
        try {
            String signStr = String.format("%s%s", methodName, returnType);
            if (args != null && args.length > 0) {
                signStr = String.format("%s%s", signStr, objectMapper.writeValueAsString(args));
            }
            return DigestUtils.md5DigestAsHex(signStr.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            //ignore
        }
        return "";
    }

    /**
     * 根据方法签名
     *
     * @param method
     * @param args
     * @return
     */
    public static String sign(Method method, Object[] args) {
        try {
//            String argStr = Arrays.stream(args).map(m -> m.getClass().getSimpleName()).collect(Collectors.joining(","));
            String argStr = Arrays.stream(method.getGenericParameterTypes()).map(m -> m.getTypeName()).collect(Collectors.joining(","));
            String signStr = String.format("public %s %s(%s)", method.getGenericReturnType(), method.getName(), argStr);
            return signStr;
        } catch (Exception e) {
            //ignore
        }
        return "";
    }

    /**
     * 简单加码
     *
     * @param obj
     * @return
     * @throws JsonProcessingException
     */
    public static byte[] encoder(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(obj);
    }

    /**
     * 简单解码
     *
     * @param bytes
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T decoder(byte[] bytes, Type type) throws IOException {
        return objectMapper.readValue(bytes, getJavaType(type));
    }

    /**
     * 简单解码
     *
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T decoder(byte[] bytes, Class<T> clazz) throws IOException {
        return objectMapper.readValue(bytes, clazz);
    }

    /**
     * 根据type转换javaType
     *
     * @param type
     * @return
     */
    public static JavaType getJavaType(Type type) {
        JavaType javaType = objectMapper.constructType(type);
        if (javaType.getRawClass() == Enum.class) {
            return objectMapper.constructType(TypeUtils.getActualTypeArguments(type));
        }
        return javaType;
    }
}
