package com.i360day.invoker.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liju.z
 */
public class Response implements Cloneable{

    private final int status;
    private final String reason;
    private final Map<String, Collection<String>> headers;
    private final Body body;

    public Response(int status, String reason, Map<String, Collection<String>> headers, Body body) {
        this.status = status;
        this.reason = reason;
        this.headers = headers;
        this.body = body;
    }

    private Response(Builder builder) {
        this.status = builder.status;
        this.reason = builder.reason;
        this.headers = builder.headers != null ? Collections.unmodifiableMap(builder.headers) : new LinkedHashMap<>();
        this.body = builder.body;
    }

    public int getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Map<String, Collection<String>> getHeaders() {
        return headers;
    }

    public Collection<String> getHeader(String name){
        return headers.get(name);
    }

    public String getContentType(){
        Collection<String> headers = getHeader("Content-Type");
        return headers != null ? headers.iterator().next() : null;
    }

    public Body getBody() {
        return body;
    }

    public HttpInvokerClientHttpResponse toClientHttpResponse(){
        return new HttpInvokerClientHttpResponse(this);
    }

    public void close() {
        if (body != null) {
            try {
                body.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }

    public Response clone(){
        return new Response(status, reason, headers, body);
    }

    public static class Builder {
        int status;
        String reason;
        Map<String, Collection<String>> headers;
        Body body;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder body(Body body) {
            this.body = body;
            return this;
        }

        public Builder body(InputStream inputStream) throws IOException {
            this.body = new InputStreamBody(inputStream, inputStream.available());
            return this;
        }

        public Builder body(InputStream inputStream, Integer length) {
            try {
                this.body = new InputStreamBody(inputStream, Optional.ofNullable(length).orElse(inputStream.available()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder setContextType(String contextType){
            return header("Content-Type", contextType);
        }

        public Builder header(Map<String, Collection<String>> headers) {
            this.headers = headers;
            return this;
        }

        public Builder header(String key, Collection<String> values) {
            if (headers == null) {
                headers = new HashMap<>();
            }
            headers.put(key, values);
            return this;
        }

        public Builder header(String key, String value) {
            if (headers == null) {
                headers = new HashMap<>();
            }
            Collection<String> values = headers.get(key);
            if (values == null) {
                values = new ArrayList<>();
            }
            values.add(value);
            headers.put(key, values);
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }


    public interface Body extends Closeable {
        /**
         * length in bytes, if known. Null if unknown or greater than {@link Integer#MAX_VALUE}.
         *
         * <br>
         * <br>
         * <br>
         * <b>Note</b><br>
         * This is an integer as most implementations cannot do bodies greater than 2GB.
         */
        int length();

        /**
         * It is the responsibility of the caller to close the stream.
         */
        InputStream asInputStream() throws IOException;
    }

    public static class InputStreamBody implements Body {

        private final InputStream inputStream;
        private final int length;

        public InputStreamBody(InputStream inputStream, int length) {
            this.inputStream = inputStream;
            this.length = length;
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public InputStream asInputStream() throws IOException {
            return inputStream;
        }

        @Override
        public void close() throws IOException {
            if (inputStream != null){
                try{
                    inputStream.close();
                }catch (Exception ex){
                    //ignore
                }
            }
        }
    }

    public static class GZIPInputStream implements Body {

        private final InputStream inputStream;
        private final int length;

        public GZIPInputStream(InputStream inputStream, int length) throws IOException {
            this.inputStream = new java.util.zip.GZIPInputStream(inputStream);
            this.length = length;
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public InputStream asInputStream() throws IOException {
            return inputStream;
        }

        @Override
        public void close() throws IOException {
            if (inputStream != null) {
                try{
                    inputStream.close();
                }catch (Exception ex){
                    //ignore
                }
            }
        }
    }

    public class HttpInvokerClientHttpResponse implements ClientHttpResponse {
        private Response response;

        public HttpInvokerClientHttpResponse(Response response) {
            this.response = response;
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return HttpStatus.valueOf(response.getStatus());
        }

//        @Override
//        public int getRawStatusCode() throws IOException {
//            return response.getStatus();
//        }

        @Override
        public String getStatusText() throws IOException {
            return HttpStatus.valueOf(response.getStatus()).toString();
        }

        @Override
        public void close() {
            response.close();
        }

        @Override
        public InputStream getBody() throws IOException {
            return response.getBody().asInputStream();
        }

        @Override
        public HttpHeaders getHeaders() {
            HttpHeaders httpHeaders = new HttpHeaders();
            response.getHeaders().forEach((key, value) -> {
                httpHeaders.addAll(key, value.stream().collect(Collectors.toList()));
            });
            return httpHeaders;
        }
    }
}
