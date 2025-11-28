package com.i360day.invoker.protocol.encoder;

/**
 *
 */
public interface ProtocolEncoder {

    /**
     * 数据包加码
     * @param obj
     * @return
     */
    byte[] encode(Object obj) throws Exception;
}
