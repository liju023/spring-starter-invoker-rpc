package com.i360day.invoker.http;

import com.i360day.invoker.properties.InvokerProperties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.*;

/**
 * @author liju.z
 */
public interface InvokerClient {
    /**
     * @param request
     * @return
     * @throws IOException
     */
    Response execute(Request request) throws IOException;

    /**
     * 关闭
     */
    void close();

    /**
     * 模式使用链接
     */
    public class Default implements InvokerClient {
        private final String HTTP_HEADER_CONTENT_ENCODING = "Content-Encoding";
        private final String ENCODING_GZIP = "gzip";
        private final SSLSocketFactory sslContextFactory;
        private final HostnameVerifier hostnameVerifier;
        private final boolean disableRequestBuffering;
        private final InvokerProperties invokerProperties;

        public Default(InvokerProperties invokerProperties) {
            this(invokerProperties, null, null);
        }
        /**
         * Create a new client, which disable request buffering by default.
         *
         * @param sslContextFactory SSLSocketFactory for secure https URL connections.
         * @param hostnameVerifier  the host name verifier.
         */
        public Default(InvokerProperties invokerProperties, SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier) {
            this(invokerProperties, sslContextFactory, hostnameVerifier, true);
        }

        /**
         * Create a new client.
         *
         * @param sslContextFactory       SSLSocketFactory for secure https URL connections.
         * @param hostnameVerifier        the host name verifier.
         * @param disableRequestBuffering Disable the request body internal buffering for
         *                                {@code HttpURLConnection}.
         */
        public Default(InvokerProperties invokerProperties, SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier, boolean disableRequestBuffering) {
            this.sslContextFactory = sslContextFactory;
            this.hostnameVerifier = hostnameVerifier;
            this.disableRequestBuffering = disableRequestBuffering;
            this.invokerProperties = invokerProperties;
        }

        @Override
        public Response execute(Request request) throws IOException {
            HttpURLConnection connection = convertAndSend(request);
            return convertResponse(connection, request);
        }

        Response convertResponse(HttpURLConnection connection, Request request) throws IOException {
            int status = connection.getResponseCode();
            String reason = connection.getResponseMessage();

            if (status < 0) {
                throw new IOException(format("Invalid status(%s) executing %s %s", status, connection.getRequestMethod(), connection.getURL()));
            }

            Map<String, Collection<String>> headers = new TreeMap<>(CASE_INSENSITIVE_ORDER);
            for (Map.Entry<String, List<String>> field : connection.getHeaderFields().entrySet()) {
                // response message
                if (field.getKey() != null) {
                    headers.put(field.getKey(), field.getValue());
                }
            }

            Integer length = connection.getContentLength();
            if (length == -1) {
                length = null;
            }
            InputStream stream;
            if (status >= 400) {
                stream = connection.getErrorStream();
            } else {
                stream = connection.getInputStream();
            }
            if (this.isGzip(headers.get(CONTENT_ENCODING))) {
                stream = new GZIPInputStream(stream);
            } else if (this.isDeflate(headers.get(CONTENT_ENCODING))) {
                stream = new InflaterInputStream(stream);
            }
            return Response.Builder.create()
                    .status(status)
                    .reason(reason)
                    .header(headers)
                    .body(stream, length)
                    .build();
        }

        public HttpURLConnection getConnection(final URL url) throws IOException {
            return (HttpURLConnection) url.openConnection();
        }

        HttpURLConnection convertAndSend(Request request) throws IOException {
            final HttpURLConnection connection = this.getConnection(request.getUri().toURL());
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection sslCon = (HttpsURLConnection) connection;
                if (sslContextFactory != null) {
                    sslCon.setSSLSocketFactory(sslContextFactory);
                }
                if (hostnameVerifier != null) {
                    sslCon.setHostnameVerifier(hostnameVerifier);
                }
            }
            connection.setConnectTimeout(invokerProperties.getRequest().getConnectTimeout());
            connection.setReadTimeout(invokerProperties.getRequest().getReadTimeout());
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");

            Collection<String> contentEncodingValues = request.headers().get(CONTENT_ENCODING);
            boolean gzipEncodedRequest = this.isGzip(contentEncodingValues);
            boolean deflateEncodedRequest = this.isDeflate(contentEncodingValues);

            boolean hasAcceptHeader = false;
            Integer contentLength = null;
            for (String field : request.headers().keySet()) {
                if (field.equalsIgnoreCase("Accept")) {
                    hasAcceptHeader = true;
                }
                for (String value : request.headers().get(field)) {
                    if (field.equals(CONTENT_LENGTH)) {
                        if (!gzipEncodedRequest && !deflateEncodedRequest) {
                            contentLength = Integer.valueOf(value);
                            connection.addRequestProperty(field, value);
                        }
                    }
                    // Avoid add "Accept-encoding" twice or more when "compression" option is enabled
                    if (field.equals(ACCEPT_ENCODING)) {
                        connection.addRequestProperty(field, String.join(", ", request.headers().get(field)));
                        break;
                    } else {
                        connection.addRequestProperty(field, value);
                    }
                }
            }
            // Some servers choke on the default accept string.
            if (!hasAcceptHeader) {
                connection.addRequestProperty("Accept", "*/*");
            }

            boolean hasEmptyBody = false;
            byte[] body = request.body().getBody();
//            if (body == null && request.httpMethod().isWithBody()) {
//                body = new byte[0];
//                hasEmptyBody = true;
//            }

            if (body != null) {
                /*
                 * Ignore disableRequestBuffering flag if the empty body was set, to ensure that internal
                 * retry logic applies to such requests.
                 */
                if (disableRequestBuffering && !hasEmptyBody) {
                    if (contentLength != null) {
                        connection.setFixedLengthStreamingMode(contentLength);
                    } else {
                        connection.setChunkedStreamingMode(8196);
                    }
                }
                connection.setDoOutput(true);
                OutputStream out = connection.getOutputStream();
                if (gzipEncodedRequest) {
                    out = new GZIPOutputStream(out);
                } else if (deflateEncodedRequest) {
                    out = new DeflaterOutputStream(out);
                }
                try {
                    out.write(body);
                } finally {
                    try {
                        out.close();
                    } catch (IOException suppressed) { // NOPMD
                    }
                }
            }
            return connection;
        }

        private boolean isGzip(Collection<String> contentEncodingValues) {
            return contentEncodingValues != null
                    && !contentEncodingValues.isEmpty()
                    && contentEncodingValues.contains(ENCODING_GZIP);
        }

        private boolean isDeflate(Collection<String> contentEncodingValues) {
            return contentEncodingValues != null
                    && !contentEncodingValues.isEmpty()
                    && contentEncodingValues.contains("deflate");
        }

        @Override
        public void close() {

        }
    }
}
