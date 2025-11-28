package com.i360day.invoker.codes.encoder;

import com.i360day.invoker.support.RemoteInvocationResult;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 * 编码器
 */
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
