package com.i360day.invoker.http;

import com.i360day.invoker.BodyTemplate;
import com.i360day.invoker.RequestTemplate;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author liju.z
 */
public class Request {

    private final URI uri;
    private final Map<String, Collection<String>> headers;
    private final BodyTemplate body;

    public Request(URI uri, Map<String, Collection<String>> headers, BodyTemplate body) {
        this.uri = uri;
        this.headers = headers;
        this.body = body;
    }

    public static Request create(URI uri, Map<String, Collection<String>> headers, BodyTemplate body, Charset charset) {
        return new Request(uri, headers, body);
    }

    public URI getUri() {
        return uri;
    }

    public Map<String, Collection<String>> headers() {
        return Collections.unmodifiableMap(headers);
    }

    public BodyTemplate body() {
        return body;
    }


    public static Request of(URI uri, RequestTemplate requestTemplate){
        return new Request(uri, requestTemplate.getHeaders(), requestTemplate.getBodyTemplate());
    }
    /**
     * Charset of the request.
     *
     * @return the current character set for the request, may be {@literal null} for binary data.
     */
    public Charset charset() {
        return Charset.forName("UTF-8");
    }


    public Request replaceUri(URI newURI) {
        return new Request(newURI, headers(), body());
    }

    @Override
    public Request clone(){
        return Request.create(getUri(), headers(), body(), charset());
    }
}
