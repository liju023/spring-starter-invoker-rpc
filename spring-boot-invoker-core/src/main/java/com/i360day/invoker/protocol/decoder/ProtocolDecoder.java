package com.i360day.invoker.protocol.decoder;

import java.lang.reflect.Type;

public interface ProtocolDecoder {

    /**
     * 数据包解码
     * @param body
     * @param type
     * @return
     * @param <T>
     */
    <T> T decode(byte [] body, Type type) throws Exception;
}
