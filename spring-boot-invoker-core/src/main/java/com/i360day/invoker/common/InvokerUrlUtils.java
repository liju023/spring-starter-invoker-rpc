package com.i360day.invoker.common;

import com.i360day.invoker.exception.InvokerException;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author liju.z
 */
public class InvokerUrlUtils {
    /**
     * 只接受协议
     */
    public final static List<String> schemeList = Arrays.asList("invoker", "http", "https", "ws", "wss", "amqp", "redis");

    /**
     * 将string url 转换为 uri
     *
     * @param url
     * @return
     */
    public static URI toUri(String url) {
        try {
            return URI.create(url);
        } catch (Exception e) {
            return URI.create(String.format("http://%s", url));
        }
    }

    /**
     * <p> 拼接URL    <p>
     *
     * <p> @author: 胡.青牛   <p>
     *
     * <p> @Date:   2019/5/10 13:58   <p>
     *
     * <p> @return:      <p>
     **/
    public static String getAssembleUrl(String... values) {
        if (ObjectUtils.isArrayEmpty(values)) {
            return "";
        }
        StringBuilder bUrl = new StringBuilder();

        for (String key : values) {
            if (ObjectUtils.isNotEmpty(key)) {
                bUrl.append(key).append("/");
            }
        }

        return bUrl.substring(0, bUrl.length() - 1);
    }

    /**
     * <p> @Description: 获取URL   <p>
     *
     * <p> @author: 胡.青牛   <p>
     *
     * <p> @Date:   2019/5/13 0013 11:22   <p>
     *
     * <p> @param null     <p>
     *
     * <p> @return:      <p>
     **/
    public static final String getServiceUrl(String url, String contextPath, Class<?> clazz) {
        if (ObjectUtils.isEmpty(contextPath)) {
            contextPath = "";
        } else if (!contextPath.startsWith("/")) {
            contextPath = InvokerConstant.SEPARATOR + contextPath;
        }
        return clazz == null ? splicingRemoteUrl(url, contextPath) : splicingRemoteUrl(url, contextPath, clazz);
    }

    /**
     * 获取客户端url
     * @param address
     * @param interfaceClazz
     * @return
     */
    public static URI getClientUrl(String address, Class<?> interfaceClazz){
        return getClientUrl(address, interfaceClazz, null, null);
    }

    /**
     * 获取客户端url
     * @param address
     * @param interfaceClazz
     * @param group
     * @return
     */
    public static URI getClientUrl(String address, Class<?> interfaceClazz, String group){
        return getClientUrl(address, interfaceClazz, group, null);
    }

    /**
     * 获取客户端url
     * @param address
     * @param interfaceClazz
     * @param group
     * @param version
     * @return
     */
    public static URI getClientUrl(String address, Class<?> interfaceClazz, String group, String version){
        Assert.isTrue(ObjectUtils.isNotEmpty(address) || !"unknown".equals(address), String.format("%s is null!", address));

        //resolve address to uri
        URI remoteClienturi = UriComponentsBuilder.fromUriString(address).userInfo(null).build().toUri();

        //协议检查
        Assert.isTrue(schemeList.contains(remoteClienturi.getScheme()), String.format("%s scheme error, Can only [%s] start with", address, schemeList.stream().collect(Collectors.joining(","))));

        //项目名
        String contextPath = remoteClienturi.getPath();
        contextPath = ObjectUtils.isEmpty(contextPath) || contextPath.startsWith("/") ? contextPath : "/" + contextPath;

        //请求地址
        String serviceUrl = InvokerUrlUtils.getAssembleUrl(remoteClienturi.toString().replaceAll(contextPath, ""), group, version);

        //重组url
        return URI.create(InvokerUrlUtils.getServiceUrl(serviceUrl, contextPath, interfaceClazz));
    }

    /**
     * <p> @Description: 获取bean命名规则   <p>
     *
     * <p> @author: 胡.青牛   <p>
     *
     * <p> @Date:   2019/5/10 0010 16:01   <p>
     *
     * <p> @param null     <p>
     *
     * <p> @return:      <p>
     **/
    private static String splicingRemoteUrl(String descUrl, String contextPath, Class<?> clazz) {
        Assert.isTrue(ObjectUtils.isNotEmpty(descUrl), "name is null");

        URI url = toUri(descUrl);
        StringBuilder urlBuilder = new StringBuilder();
        int port = url.getPort();
        if (ObjectUtils.isEmpty(url.getPath())) {
            return urlBuilder
                    .append(url)
                    .append(appendUrl(
                            contextPath,
                            InvokerConstant.FIXED_URL,
                            clazz == null ? "" : ClassUtils.getClassNameToLowerCaseFirstOne(clazz.getSimpleName())
                    ))
                    .toString();
        }
        return urlBuilder
                .append(url.getScheme()).append("://").append(url.getHost()).append(port < 0 ? "" : ":" + port)
                .append(appendUrl(
                        contextPath,
                        InvokerConstant.FIXED_URL,
                        url.getPath(),
                        clazz == null ? "" : ClassUtils.getClassNameToLowerCaseFirstOne(clazz.getSimpleName())
                ))
                .toString();
    }


    /**
     * <p> @Description: 获取bean命名规则   <p>
     *
     * <p> @author: 胡.青牛   <p>
     *
     * <p> @Date:   2019/5/10 0010 16:01   <p>
     *
     * <p> @param null     <p>
     *
     * <p> @return:      <p>
     **/
    public static final String splicingRemoteUrl(String descUrl, String contextPath) {
        return splicingRemoteUrl(descUrl, contextPath, null);
    }

    /**
     * 替换url地址
     *
     * @param descUrl
     * @return
     */
    public static String replaceUrl(String descUrl) {
        if (ObjectUtils.isEmpty(descUrl)) return descUrl;

        return descUrl.replaceAll("\\\\", "/").replaceAll("////", "/").replaceAll("///", "/").replaceAll("//", "/");
    }

    /**
     * <p> @author liju.z <p>
     *
     * <p> @Description 拼接 <p>
     *
     * <p> @Date  17:11 <p>
     *
     * <p> @Param [urls] <p>
     **/
    private static String appendUrl(Object... urls) {
        StringBuilder builder = new StringBuilder();
        for (Object url : urls) {
            if (ObjectUtils.isNotEmpty(url)) {
                builder.append(url).append("/");
            }
        }
        return replaceUrl(builder.substring(0, builder.length() - 1));
    }

    /**
     * <p> @Description: 获取端口 <p>
     *
     * <p> @author: 胡.青牛 <p>
     *
     * <p> @Date:   2019/6/13 0013 17:48 <p>
     *
     * <p> @param null   </p>
     *
     * <p> @return:    <p>
     **/
    public static int getPort(String descUrl) {
        if (ObjectUtils.isEmpty(descUrl)) {
            throw new InvokerException("descUrl is null");
        }

        URI url = toUri(descUrl);
        if (url.getPort() <= 0) {
            if (descUrl.startsWith("http://") || descUrl.startsWith("ws://")) {
                return 80;
            } else if (descUrl.startsWith("https://") || descUrl.startsWith("wss://")) {
                return 443;
            }
            return 80;
        }
        return url.getPort();
    }

    /**
     * 获取IP
     *
     * @param descUrl
     * @return
     */
    public static String getIp(String descUrl) {
        if (ObjectUtils.isEmpty(descUrl)) {
            throw new InvokerException("descUrl is null");
        }

        URI url = toUri(descUrl);
        return url.getHost();
    }

    /**
     * 获取网络地址
     *
     * @param url
     * @return
     */
    public static String getNetworkAddress(String url) {
        return getNetworkAddress(toUri(url));
    }

    /**
     * 获取网络地址
     *
     * @param uri
     * @return
     */
    public static String getNetworkAddress(URI uri) {
        StringBuilder builder = new StringBuilder(uri.getHost());
        if (uri.getPort() > 0) {
            builder.append(":").append(uri.getPort());
        }
        return builder.toString();
    }

    /**
     * 指定ip替换url
     *
     * @param uri
     * @param ip
     * @return
     */
    public static URI reconstructURIWithIp(String uri, String ip) {
        return reconstructURIWithIp(URI.create(uri), ip);
    }

    /**
     * 指定ip替换url
     *
     * @param uri
     * @param ip
     * @return
     */
    public static URI reconstructURIWithIp(URI uri, String ip) {
        return reconstructURIWithIp(uri, ip, "");
    }

    /**
     * <p> @author liju.z <p>
     *
     * <p> @Description 指定ip项目名替换 <p>
     *
     * <p> @Date  16:57 <p>
     *
     * <p> @Param [uri, ip, contextPath] <p>
     *
     * <p> @return [uri, ip, contextPath] <p>
     **/
    public static URI reconstructURIWithIp(URI uri, String ip, String contextPath) {
        String scheme = uri.getScheme();
        if (null == scheme) {
            if (uri.toString().startsWith("/")) {
                scheme = "http";
            } else {
                if (uri.getPort() <= 0) {
                    if (uri.toString().startsWith("http://")) {
                        scheme = "http";
                    } else if (uri.toString().startsWith("https://")) {
                        scheme = "https";
                    }
                } else {
                    scheme = scheme.startsWith("http") ? scheme : "http";
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://").append(ip).append(contextPath).append(uri.getPath());
        if (ObjectUtils.isNotEmpty(uri.getRawQuery())) {
            sb.append("?").append(uri.getRawQuery());
        }
        return URI.create(sb.toString());
    }

    /**
     * 将url转换成驼峰命名
     *
     * @param url
     * @return
     */
    public static String urlToCamelBeanName(String url) {
        String[] split = replaceUrl(url).split("\\/");
        if (split.length == 0) return url;

        String className = IntStream.range(0, split.length)
                .mapToObj(i -> ClassUtils.getClassNameToUpperCaseFirstOne(split[i]))
                .filter(f -> ObjectUtils.isNotEmpty(f))
                .collect(Collectors.joining(""));

        return ClassUtils.getClassNameToUpperCaseFirstOne(className);
    }


    /**
     * http://invoker:invoker@127.0.0.1:6379/test/query
     *
     * @param url
     * @param regex
     * @return
     */
    public static List<NetworkAddress> resolveNetworkAddressList(String url, String regex) {
        return Arrays.asList(url.split(regex)).stream().distinct().map(address -> {
            //
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(address);
            //
            String userInfo = uriComponentsBuilder.build().getUserInfo();
            String[] spUserInfoStr = userInfo != null ? userInfo.split(":") : new String[]{};

            URI uri = uriComponentsBuilder.userInfo(null).build().toUri();
            if ("unknown".equals(uri.getAuthority())) return null;

            return new NetworkAddress(
                    uri.getHost(),
                    uri.getPort(),
                    spUserInfoStr.length >= 1 ? spUserInfoStr[0] == "invoker" ? null : spUserInfoStr[0] : null,
                    spUserInfoStr.length >= 2 ? spUserInfoStr[1] == "invoker" ? null : spUserInfoStr[1] : null
            );
        }).filter(f -> f != null).collect(Collectors.toList());
    }

    /**
     * 网络配置信息
     *
     * @param host
     * @param port
     * @param username
     * @param password
     */
    public record NetworkAddress(String host, int port, String username, String password) {
    }
}
