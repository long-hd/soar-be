package com.hdl.soar.framework.common.util.servlet;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Client utility class
 */
public class ServletUtils {

    /**
     * Returns a JSON string.
     *
     * @param response the response
     * @param object   the object to be serialized into a JSON string
     */
    public static void writeJSON(HttpServletResponse response, Object object) {
        String content = JsonUtils.toJsonString(object);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            response.getWriter().write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a file attachment.
     *
     * @param response the response
     * @param filename the file name
     * @param content  the attachment content
     */
    public static void writeAttachment(HttpServletResponse response, String filename, byte[] content) throws IOException {
        // Set headers and content type
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        // Output attachment
        IoUtil.write(response.getOutputStream(), false, content);
    }

    /**
     * @param request the request
     * @return user agent (UA)
     */
    public static String getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return ua != null ? ua : "";
    }

    /**
     * Gets the current request.
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    public static String getUserAgent() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return getUserAgent(request);
    }

    public static String getClientIP() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return JakartaServletUtil.getClientIP(request);
    }

    public static boolean isJsonRequest(ServletRequest request) {
        return StrUtil.startWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE);
    }

    public static String getBody(HttpServletRequest request) {
        // Only read for JSON requests, because only CacheRequestBodyFilter caches the body and supports repeated reading
        if (isJsonRequest(request)) {
            return JakartaServletUtil.getBody(request);
        }
        return null;
    }

    public static byte[] getBodyBytes(HttpServletRequest request) {
        // Only read for JSON requests, because only CacheRequestBodyFilter caches the body and supports repeated reading
        if (isJsonRequest(request)) {
            return JakartaServletUtil.getBodyBytes(request);
        }
        return null;
    }

    public static String getClientIP(HttpServletRequest request) {
        return JakartaServletUtil.getClientIP(request);
    }

    public static Map<String, String> getParamMap(HttpServletRequest request) {
        return JakartaServletUtil.getParamMap(request);
    }

    public static Map<String, String> getHeaderMap(HttpServletRequest request) {
        return JakartaServletUtil.getHeaderMap(request);
    }
}
