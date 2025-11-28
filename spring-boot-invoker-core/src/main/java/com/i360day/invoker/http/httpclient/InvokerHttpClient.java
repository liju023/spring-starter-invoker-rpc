/*
 * Copyright (c) 1994, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.i360day.invoker.http.httpclient;

import com.i360day.invoker.BodyTemplate;
import com.i360day.invoker.http.InvokerClient;
import com.i360day.invoker.http.Request;
import com.i360day.invoker.http.Response;
import com.i360day.invoker.properties.InvokerProperties;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: liju.z
 * @date: 2024/2/5 3:47
 */
public class InvokerHttpClient implements InvokerClient {

    private Logger logger = LoggerFactory.getLogger(InvokerHttpClient.class);

    private final String HTTP_HEADER_CONTENT_ENCODING = "Content-Encoding";

    private final String ENCODING_GZIP = "gzip";

    private CloseableHttpClient httpClient;
    /**
     * 默认最大总连接数
     */
    private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 100;
    /**
     * 每条路由的默认最大连接数
     */
    private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 10;
    private final PlainConnectionSocketFactory plainConnectionSocketFactory;
    private final SSLConnectionSocketFactory sslConnectionSocketFactory;
    private final RequestConfig requestConfig;

    public static InvokerHttpClient create(InvokerProperties properties) {
        return new InvokerHttpClient(properties);
    }


    public InvokerHttpClient(InvokerProperties properties) {
        this(PlainConnectionSocketFactory.getSocketFactory(), SSLConnectionSocketFactory.getSocketFactory(), properties);
    }

    public InvokerHttpClient(PlainConnectionSocketFactory plainConnectionSocketFactory, SSLConnectionSocketFactory sslConnectionSocketFactory, InvokerProperties properties) {
        this.plainConnectionSocketFactory = plainConnectionSocketFactory == null ? PlainConnectionSocketFactory.getSocketFactory() : plainConnectionSocketFactory;
        this.sslConnectionSocketFactory = sslConnectionSocketFactory == null ? SSLConnectionSocketFactory.getSocketFactory() : sslConnectionSocketFactory;
        this.requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(properties.getRequest().getConnectTimeout(), TimeUnit.MILLISECONDS))
                .setConnectionRequestTimeout(Timeout.of(properties.getRequest().getConnectionRequestTimeout(), TimeUnit.MILLISECONDS))
                .setResponseTimeout(Timeout.of(properties.getRequest().getReadTimeout(), TimeUnit.MILLISECONDS))
                .build();

        InvokerProperties.HttpInvokerRequestProperties request = properties.getRequest();

        Registry<ConnectionSocketFactory> schemeRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", this.plainConnectionSocketFactory)
                .register("https", !properties.isSecure() ? new SSLConnectionSocketFactory(createIgnoreVerifySSL()) : this.sslConnectionSocketFactory)
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(schemeRegistry);
        connectionManager.setMaxTotal(request.getMaxConnections());
        connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
        httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager).build();
    }

    private SSLContext createIgnoreVerifySSL() {
        try{
            SSLContext sc = SSLContext.getInstance("SSL");
            // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
            X509TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate, String paramString) {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate, String paramString) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sc.init(null, new TrustManager[] { trustManager }, new SecureRandom());
            return sc;
        }catch (Exception ex){
            logger.warn("Error setting SSLSocketFactory in HttpClient", ex);
        }
        return null;
    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            //ignore
        }
    }

    /**
     * 执行
     *
     * @param request
     * @return
     * @throws IOException
     */
    @Override
    public Response execute(Request request) throws IOException {
        //request
        CloseableHttpResponse httpResponse = convertAndSend(request);
        validateResponse(httpResponse);
        //body
        InputStream inputStream = httpResponse.getEntity().getContent();
        Response.Body body;
        if (isGzipResponse(httpResponse)) {
            body = new Response.GZIPInputStream(inputStream, inputStream.available());
        } else {
            body = new Response.InputStreamBody(inputStream, inputStream.available());
        }
        //builder
        Response.Builder builder = Response.Builder.create();
        //header
        Header[] allHeaders = httpResponse.getHeaders();
        for (Header header : allHeaders) {
            builder.header(header.getName(), header.getValue());
        }
        //response
        return builder
                .status(httpResponse.getCode())
                .reason(httpResponse.getReasonPhrase())
                .body(body)
                .build();
    }

    /**
     * 识别response
     *
     * @param httpResponse
     * @return
     */
    boolean isGzipResponse(HttpResponse httpResponse) {
        Header encodingHeader = httpResponse.getFirstHeader(HTTP_HEADER_CONTENT_ENCODING);
        return (encodingHeader != null && encodingHeader.getValue() != null &&
                encodingHeader.getValue().toLowerCase().contains(ENCODING_GZIP));
    }

    /**
     * 验证response
     *
     * @param response
     * @throws IOException
     */
    void validateResponse(CloseableHttpResponse response) throws IOException {
        int status = response.getCode();
        if (status >= 300) {
            String serverErrorContent = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
            logger.error("http invoker server error -> {}", serverErrorContent);
            throw new NoHttpResponseException(
                    String.format(
                            "Did not receive successful HTTP response: status code = %s status message = [%s] %s !",
                            status,
                            response.getReasonPhrase(),
                            serverErrorContent
                    )
            );
        }
    }

    /**
     * 发送请求
     *
     * @param request
     * @return
     * @throws IOException
     */
    CloseableHttpResponse convertAndSend(Request request) throws IOException {

        ClassicHttpRequest httpRequest = convertHttpRequest(request, requestConfig);

        return httpClient.execute(httpRequest);
    }

    /**
     * @param request
     * @return
     */
    ClassicHttpRequest convertHttpRequest(Request request, RequestConfig config) {
        //request
        HttpPost httpPost = new HttpPost(request.getUri());
        //header
        Map<String, Collection<String>> headers = request.headers();
        headers.forEach((name, values) -> {
            if (values != null && values.size() > 0) {
                values.forEach(value -> {
                    httpPost.addHeader(name, value);
                });
            } else {
                httpPost.addHeader(name, "");
            }
        });
        //data
        BodyTemplate body = request.body();
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(body.getBody(), ContentType.APPLICATION_OCTET_STREAM);

        httpPost.setConfig(requestConfig);
        httpPost.setEntity(byteArrayEntity);
        //body
        return httpPost;
    }
}
