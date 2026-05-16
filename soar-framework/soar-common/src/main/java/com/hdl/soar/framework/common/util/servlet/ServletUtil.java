package com.hdl.soar.framework.common.util.servlet;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ServletUtil {

    /**
     * Gets the client IP address.
     *
     * <p>
     * Default headers checked:
     *
     * <pre>
     * 1. X-Forwarded-For
     * 2. X-Real-IP
     * 3. Proxy-Client-IP
     * 4. WL-Proxy-Client-IP
     * </pre>
     *
     * <p>
     * The parameter otherHeaderNames is used to define additional custom headers.
     * Note that when using this method to obtain the client IP address, the corresponding
     * headers must be configured on the HTTP server (e.g., Nginx), otherwise IP spoofing may occur.
     * </p>
     *
     * @param request          the HttpServletRequest object
     * @param otherHeaderNames additional custom headers, typically configured in the HTTP server (e.g., Nginx)
     * @return the client IP address
     */
    public static String getClientIP(final HttpServletRequest request, final String... otherHeaderNames) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        if (ArrayUtil.isNotEmpty(otherHeaderNames)) {
            headers = ArrayUtil.addAll(headers, otherHeaderNames);
        }

        return getClientIPByHeader(request, headers);
    }

    /**
     * Gets the client IP address.
     *
     * <p>
     * The parameter headerNames is used to define custom headers.
     * Note that when using this method to obtain the client IP address, the corresponding
     * headers must be configured on the HTTP server (e.g., Nginx), otherwise IP spoofing may occur.
     * </p>
     *
     * @param request     the HttpServletRequest object
     * @param headerNames custom headers, typically configured in the HTTP server (e.g., Nginx)
     * @return the client IP address
     * @since 4.4.1
     */
    public static String getClientIPByHeader(final HttpServletRequest request, final String... headerNames) {
        String ip;
        for (final String header : headerNames) {
            ip = request.getHeader(header);
            if (!NetUtil.isUnknown(ip)) {
                return NetUtil.getMultistageReverseProxyIp(ip);
            }
        }

        ip = request.getRemoteAddr();
        return NetUtil.getMultistageReverseProxyIp(ip);
    }

    // --------------------------------------------------------- getParam start

    /**
     * Gets all request parameters.
     *
     * @param request the request object {@link ServletRequest}
     * @return a Map
     */
    public static Map<String, String[]> getParams(final ServletRequest request) {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * Gets all request parameters.
     *
     * @param request the request object {@link ServletRequest}
     * @return a Map
     */
    public static Map<String, String> getParamMap(final ServletRequest request) {
        final Map<String, String> params = new HashMap<>();
        for (final Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
            params.put(entry.getKey(), ArrayUtil.join(entry.getValue(), StrUtil.COMMA));
        }
        return params;
    }

    /**
     * Gets the request body as a String.<br>
     * After calling this method, getParam methods will no longer work.
     *
     * @param request the {@link ServletRequest}
     * @return the request body
     * @since 4.0.2
     */
    public static String getBody(final ServletRequest request) {
        try (final BufferedReader reader = request.getReader()) {
            return IoUtil.read(reader);
        } catch (final IOException e) {
            throw new IORuntimeException(e);
        }
    }

    /**
     * Gets the request body as a byte array.<br>
     * After calling this method, getParam methods will no longer work.
     *
     * @param request the {@link ServletRequest}
     * @return the request body as byte[]
     * @since 4.0.2
     */
    public static byte[] getBodyBytes(final ServletRequest request) {
        try {
            return IoUtil.readBytes(request.getInputStream());
        } catch (final IOException e) {
            throw new IORuntimeException(e);
        }
    }

    // --------------------------------------------------------- Header start

    /**
     * Gets all request headers.
     *
     * @param request the request object {@link HttpServletRequest}
     * @return header values
     * @since 4.6.2
     */
    public static Map<String, String> getHeaderMap(final HttpServletRequest request) {
        final Map<String, String> headerMap = new HashMap<>();

        final Enumeration<String> names = request.getHeaderNames();
        String name;
        while (names.hasMoreElements()) {
            name = names.nextElement();
            headerMap.put(name, request.getHeader(name));
        }

        return headerMap;
    }


}
