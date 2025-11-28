package com.i360day.invoker.codes.encoder;

import com.i360day.invoker.common.InvokerConstant;
import com.i360day.invoker.support.RemoteInvocationResult;
import com.i360day.invoker.support.SimpleHttpOutputMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class SpringEncoder implements Encoder {
    private final Log logger = LogFactory.getLog(SpringEncoder.class);
    private final ObjectFactory<HttpMessageConverters> messageConverters;
    /**
     * 指定使用application/json头编码
     */
    private final MediaType contentType =  MediaType.parseMediaType(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER);

    /**
     * spring 转换
     * @param messageConverters
     */
    public SpringEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        this.messageConverters = messageConverters;
    }

    /**
     * 编码
     * @param outputStream
     * @param responseType
     * @param result
     * @throws IOException
     */
    @Override
    public void encoder(OutputStream outputStream, Type responseType, RemoteInvocationResult result) throws IOException {
        write(result.getValue(), responseType, contentType, new SimpleHttpOutputMessage(outputStream, contentType));
    }

    /**
     * out message
     *
     * @param data
     * @param responseType
     * @param mediaType
     * @param httpOutputMessage
     * @throws IOException
     */
    private void write(Object data, Type responseType, MediaType mediaType, HttpOutputMessage httpOutputMessage) throws IOException {
        List<HttpMessageConverter<?>> converters = messageConverters.getObject().getConverters();
        for (HttpMessageConverter<?> messageConverter : converters) {
            if (messageConverter instanceof GenericHttpMessageConverter) {
                GenericHttpMessageConverter<Object> genericMessageConverter = (GenericHttpMessageConverter<Object>) messageConverter;
                if (genericMessageConverter.canRead(responseType, null, mediaType)) {
                    if (logger.isDebugEnabled()) {
                        ResolvableType resolvableType = ResolvableType.forType(responseType);
                        logger.debug("Reading to [" + resolvableType + "]");
                    }
                    genericMessageConverter.write(Optional.ofNullable(data).orElse("null"), responseType, mediaType, httpOutputMessage);
                    return;
                }
            }
            if (responseType != null) {
                if (messageConverter.canRead(responseType.getClass(), mediaType)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Reading to [" + responseType.getTypeName() + "] as \"" + mediaType + "\"");
                    }
                    ((HttpMessageConverter<Object>) messageConverter).write(Optional.ofNullable(data).orElse(""), mediaType, httpOutputMessage);
                    return;
                }
            }
        }
    }
}
