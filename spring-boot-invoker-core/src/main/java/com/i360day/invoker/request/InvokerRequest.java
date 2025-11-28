package com.i360day.invoker.request;

import com.i360day.invoker.RequestTemplate;
import com.i360day.invoker.http.Response;

import java.io.IOException;

/**
 * 请求接口
 * @author liju.z
 */
public interface InvokerRequest {

    /**
     * <p> @author liju.z <p>
     *
     * <p> @Description 执行请求 <p>
     *
     * <p> @Date  20:52 <p>
     *
     * <p> @Param [requestTemplate, options] <p>
     *
     * <p> @return [requestTemplate, options] <p>
     *
     **/
    Response executor(RequestTemplate requestTemplate) throws IOException;
}
