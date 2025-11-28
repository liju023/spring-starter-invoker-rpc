package com.i360day.invoker.codes.decoder;

import com.i360day.invoker.http.Response;
import com.i360day.invoker.support.RemoteInvocationResult;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 解码器
 * @author liju.z
 */
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
