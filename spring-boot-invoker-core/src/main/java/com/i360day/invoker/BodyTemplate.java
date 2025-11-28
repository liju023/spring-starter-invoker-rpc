package com.i360day.invoker;

/**
 * request body
 *
 * @author liju.z
 */
public interface BodyTemplate {
    /**
     * 获取body字节
     * @return
     */
    byte[] getBody();

    /**
     * 获取body长度
     * @return
     */
    int getSize();
}
