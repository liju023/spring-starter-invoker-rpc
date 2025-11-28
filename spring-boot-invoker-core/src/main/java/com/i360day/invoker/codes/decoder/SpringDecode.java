package com.i360day.invoker.codes.decoder;

import com.i360day.invoker.common.InvokerConstant;
import com.i360day.invoker.exception.InvalidContentTypeException;
import com.i360day.invoker.http.Response;
import com.i360day.invoker.support.RemoteInvocationResult;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpMessageConverterExtractor;

import java.lang.reflect.Type;

public class SpringDecode implements Decoder {
    /**
     * 只接受的MediaType
     */
    private final MediaType acceptContentType = MediaType.parseMediaType(InvokerConstant.ACCEPT_RPC_HTTP_INVOKER);

    /**
     * spring HttpMessageConverters
     */
    private ObjectFactory<HttpMessageConverters> messageConverters;

    /**
     * spring 解密转换
     * @param messageConverters
     */
    public SpringDecode(ObjectFactory<HttpMessageConverters> messageConverters) {
        this.messageConverters = messageConverters;
    }

    /**
     * 将response中的流解码成RemoteInvocationResult
     * @param response
     * @param type
     * @return
     */
    @Override
    public RemoteInvocationResult decode(Response response, Type type) {
        try {
            if (!acceptContentType.toString().equals(response.getContentType())) {
                throw new InvalidContentTypeException(String.format("The response [%s] does not match the context [%s]", acceptContentType, response.getContentType()));
            }

            //result input Stream
            HttpMessageConverterExtractor httpMessageConverterExtractor = new HttpMessageConverterExtractor(type, messageConverters.getObject().getConverters());

            //Result setn
            return new RemoteInvocationResult(httpMessageConverterExtractor.extractData(response.toClientHttpResponse()));
        }catch (Exception ex){
            return new RemoteInvocationResult(ex);
        }finally {
            response.close();
        }
    }
}
