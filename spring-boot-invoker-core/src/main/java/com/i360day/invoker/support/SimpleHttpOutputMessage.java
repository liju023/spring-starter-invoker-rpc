package com.i360day.invoker.support;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Optional;

public class SimpleHttpOutputMessage implements HttpOutputMessage {

        private HttpServletResponse response;
        private MediaType mediaType;
        private OutputStream outputStream;

        public SimpleHttpOutputMessage(HttpServletResponse response, MediaType mediaType) throws IOException {
            this.response = response;
            this.mediaType = mediaType;
        }

        public SimpleHttpOutputMessage(OutputStream outputStream, MediaType mediaType) {
            this.outputStream = outputStream;
            this.mediaType = mediaType;
        }

        @Override
        public OutputStream getBody() throws IOException {
            if(outputStream != null){
                return outputStream;
            }
            return response.getOutputStream();
        }

        @Override
        public HttpHeaders getHeaders() {
            HttpHeaders httpHeaders = new HttpHeaders();
            Optional.ofNullable(response).ifPresent(r -> {
                Iterator<String> iterator = r.getHeaderNames().iterator();
                while(iterator.hasNext()){
                    String name = iterator.next();
                    httpHeaders.add(name, r.getHeader(name));
                }
            });
            httpHeaders.setContentType(mediaType);
            return httpHeaders;
        }
    }