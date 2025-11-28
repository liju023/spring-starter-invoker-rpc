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
package com.i360day.invoker.http.okhttp;

import com.i360day.invoker.common.ObjectUtils;
import com.i360day.invoker.http.InvokerClient;
import com.i360day.invoker.http.Request;
import com.i360day.invoker.http.Response;
import com.i360day.invoker.http.httpclient.InvokerHttpClient;
import com.i360day.invoker.properties.InvokerProperties;
import kotlin.Pair;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author: liju.z
 * @date: 2024/2/5 3:49
 */
public class InvokerOkHttpClient implements InvokerClient {

    private Logger logger = LoggerFactory.getLogger(InvokerHttpClient.class);

    private final String HTTP_HEADER_CONTENT_ENCODING = "Content-Encoding";

    private final String ENCODING_GZIP = "gzip";

    private OkHttpClient okHttpClient;

    public static InvokerOkHttpClient create(InvokerProperties properties) {
        return new InvokerOkHttpClient(properties);
    }


    public InvokerOkHttpClient(InvokerProperties properties) {
        InvokerProperties.HttpInvokerRequestProperties request = properties.getRequest();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(properties.getRequest().getConnectTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(properties.getRequest().getConnectionRequestTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getRequest().getReadTimeout(), TimeUnit.MILLISECONDS)
                .callTimeout(properties.getRequest().getConnectTimeout(), TimeUnit.MILLISECONDS)
                .connectionPool(new ConnectionPool(request.getMaxConnections(), request.getTimeToLive(), TimeUnit.MILLISECONDS))
                .followRedirects(false)
                .followSslRedirects(false)
                ;
        this.okHttpClient = !properties.isSecure() ? disableSsl(builder).build() : builder.build();
    }

    private OkHttpClient.Builder disableSsl(OkHttpClient.Builder builder) {
        try {
            X509TrustManager disabledTrustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{disabledTrustManager}, new java.security.SecureRandom());
            SSLSocketFactory disabledSSLSocketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(disabledSSLSocketFactory, disabledTrustManager);
            builder.hostnameVerifier((s, sslSession) -> true);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.warn("Error setting SSLSocketFactory in OKHttpClient", e);
        }
        return builder;
    }

    @Override
    public Response execute(Request request) throws IOException {
        okhttp3.Request.Builder builderRequest = new okhttp3.Request.Builder().url(request.getUri().toURL());

        request.headers().forEach((key, value) -> builderRequest.addHeader(key, value.iterator().next()));

        RequestBody requestBody = RequestBody.create(request.body().getBody());

        okhttp3.Request okHttpRequest = builderRequest.post(requestBody).build();

//        try () {
        okhttp3.Response response = this.okHttpClient.newCall(okHttpRequest).execute();
        //body
        Response.Body body;
        InputStream inputStream = response.body().byteStream();
        if (ObjectUtils.isEquals(response.headers().get(HTTP_HEADER_CONTENT_ENCODING), ENCODING_GZIP)) {
            body = new Response.GZIPInputStream(inputStream, inputStream.available());
        } else {
            body = new Response.InputStreamBody(inputStream, inputStream.available());
        }

        //header
        Response.Builder builder = Response.Builder.create();
        Iterator<Pair<String, String>> iterator = response.headers().iterator();
        while (iterator.hasNext()) {
            Pair<String, String> pair = iterator.next();
            builder.header(pair.component1(), pair.component2());
        }

        //result
        return builder
                .body(body)
                .status(response.code())
                .build();
//        }
    }

    @Override
    public void close() {
        Dispatcher dispatcher = this.okHttpClient.dispatcher();
        Optional.ofNullable(dispatcher).ifPresent(o -> o.cancelAll());
        Optional.ofNullable(dispatcher.executorService()).ifPresent(o -> o.shutdown());
        Optional.ofNullable(this.okHttpClient.connectionPool()).ifPresent(o -> o.evictAll());
    }
}
