package com.i360day.invoker;

import com.i360day.invoker.common.InvokerConstant;
import com.i360day.invoker.common.ObjectUtils;
import com.i360day.invoker.http.Request;
import com.i360day.invoker.proxy.TargetProxy;
import com.i360day.invoker.support.RemoteInvocation;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

/**
 * request template
 */
public class RequestTemplate {
    private URI uri;
    private String path;
    private URI basePath;
    private TargetProxy targetProxy;
    private BodyTemplate bodyTemplate;
    private MethodMetadata methodMetadata;
    private transient Charset charset = Charset.forName("UTF-8");
    private final Map<String, Collection<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * @param targetProxy
     * @param methodMetadata
     * @param bodyTemplate
     * @throws IOException
     */
    private RequestTemplate(TargetProxy targetProxy, MethodMetadata methodMetadata, BodyTemplate bodyTemplate) throws IOException {
        this.methodMetadata = methodMetadata;
        this.bodyTemplate = bodyTemplate;
        this.targetProxy = targetProxy;
        this.uri = targetProxy.getUrl();

        //resolve path
        String path = getUri().toString();
        this.path = path.substring(path.indexOf(InvokerConstant.FIXED_URL));

        //resolve base path
        if (path.indexOf(InvokerConstant.FIXED_URL) >= 0) {
            this.basePath = URI.create(path.substring(0, path.indexOf(InvokerConstant.FIXED_URL)));
        } else {
            this.basePath = URI.create(String.format("%s://%s", uri.getScheme(), uri.getAuthority()));
        }

        //client account
        addHeader(InvokerConstant.AUTHORIZATION, String.format("%s:%s", targetProxy.getClientUser(), targetProxy.getClientPassword()));
    }

    /**
     * 添加header
     *
     * @param name
     * @param value
     * @return
     */
    public RequestTemplate addHeader(String name, String value) {
        if (ObjectUtils.isEmpty(name) || ObjectUtils.isEmpty(value)) return this;
        return addHeaders(name, Arrays.asList(value));
    }

    /**
     * 添加header
     *
     * @param key
     * @param values
     * @return
     */
    public RequestTemplate addHeaders(String key, Collection<String> values) {
        Collection<String> originHeaderValue = headers.get(key);
        if (originHeaderValue == null) {
            originHeaderValue = new ArrayList<>(Optional.ofNullable(values).orElseGet(() -> new ArrayList<>()));
        } else {
            originHeaderValue.addAll(values);
        }
        headers.put(key, originHeaderValue);
        return this;
    }

    /**
     * 设置content-type
     *
     * @param contentType
     * @return
     */
    public RequestTemplate setContentType(String contentType) {
        addHeader("Content-type", contentType);
        return this;
    }

    /**
     * get header info
     *
     * @return
     */
    public Map<String, Collection<String>> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * get name to header value
     *
     * @param name
     * @return
     */
    public String getHeader(String name) {
        Collection<String> values = Collections.unmodifiableMap(headers).get(name);
        return values.iterator().next();
    }

    /**
     * get name to header values
     *
     * @param name
     * @return
     */
    public Collection<String> getHeaders(String name) {
        return Collections.unmodifiableMap(headers).get(name);
    }

    /**
     * get charset
     *
     * @return
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * get method meta data
     *
     * @return
     */
    public MethodMetadata getMethodMetadata() {
        return methodMetadata;
    }

    /**
     * get request body template
     *
     * @return
     */
    public BodyTemplate getBodyTemplate() {
        return bodyTemplate;
    }

    /**
     * get request uri address
     *
     * @return
     */
    public URI getUri() {
        return uri;
    }

    /**
     * get request path
     *
     * @return
     */
    public String getRequestPath() {
        return this.path;
    }

    /**
     * get request authority
     *
     * @return
     */
    public String getRequestAuthority() {
        //resolve path
        return this.getUri().getAuthority();
    }

    /**
     * get request base path
     *
     * @return
     */
    public URI getRequestBasePath() {
        return this.basePath;
    }

    /**
     * 代理的目标对象
     *
     * @return
     */
    public TargetProxy getTargetProxy() {
        return targetProxy;
    }

    public static RequestTemplate of(TargetProxy targetProxy, MethodMetadata methodMetadata, BodyTemplate bodyTemplate) throws IOException {
        return new RequestTemplate(targetProxy, methodMetadata, bodyTemplate);
    }

    public static RequestTemplate of(TargetProxy targetProxy, MethodInvocation invocation, RemoteInvocation remoteInvocation) throws IOException {
        return new RequestTemplate(targetProxy, MethodMetadata.of(invocation), ObjectBodyTemplate.of(remoteInvocation));
    }

    public static RequestTemplate of(TargetProxy targetProxy, MethodMetadata methodMetadata, RemoteInvocation remoteInvocation) throws IOException {
        return new RequestTemplate(targetProxy, methodMetadata, ObjectBodyTemplate.of(remoteInvocation));
    }

    public Request convertRequest() {
        URI uri = UriComponentsBuilder.fromUri(getUri()).userInfo(null).build().toUri();
        return Request.of(uri, this);
    }

    /**
     * copy token default Authorization key
     *
     * @return
     */
    public boolean copyAuthorizationToken() {
        return copyAuthorizationToken("Authorization");
    }

    /**
     * copy token
     *
     * @param tokenKey 指定头字段
     * @return
     */
    public boolean copyAuthorizationToken(String tokenKey) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes servletWebRequest = ( ServletRequestAttributes ) requestAttributes;
            String value = servletWebRequest.getRequest().getHeader(tokenKey);
            if (ObjectUtils.isNotEmpty(value)) {
                addHeader(tokenKey, value);
                return true;
            }
        }
        return false;
    }
}
