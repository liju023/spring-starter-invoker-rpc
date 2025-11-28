package com.i360day.invoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.i360day.invoker.support.RemoteInvocation;

import java.io.IOException;

/**
 * java object
 * @author: liju.z
 * @create: 2022-08-14 11:59
 **/
public class JsonBodyTemplate implements BodyTemplate{

    private byte[] body;

    public JsonBodyTemplate(ObjectMapper objectMapper, RemoteInvocation remoteInvocation) throws IOException {
        this.body = objectMapper.writeValueAsBytes(remoteInvocation);
    }

    public static JsonBodyTemplate of(ObjectMapper objectMapper, RemoteInvocation remoteInvocation) throws IOException {
        return new JsonBodyTemplate(objectMapper, remoteInvocation);
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public int getSize() {
        return body.length;
    }
}
