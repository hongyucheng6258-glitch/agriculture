package com.aquaculture.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 修复中文参数编码问题
 *
 * 根本原因：vue-cli 的 http-proxy-middleware 会自动将 URL 编码的中文参数解码为原始中文字符，
 * 然后转发给 Tomcat。Tomcat 9 默认不允许 URL 中包含原始 UTF-8 字符，会返回 400 错误。
 *
 * 解决方案：在 Servlet Filter 中检测请求 URL 是否包含非 ASCII 字符，
 * 如果有，则将查询参数重新编码为 URL 编码格式，然后包装为新的请求对象。
 */
@Configuration
public class ChineseParamFilterConfig {

    @Bean
    public FilterRegistrationBean<ChineseParamFilter> chineseParamFilter() {
        FilterRegistrationBean<ChineseParamFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ChineseParamFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("chineseParamFilter");
        return registration;
    }

    public static class ChineseParamFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String queryString = httpRequest.getQueryString();

            if (queryString != null && containsNonAscii(queryString)) {
                // 查询参数中包含非 ASCII 字符（如中文），需要重新编码
                chain.doFilter(new EncodedQueryRequestWrapper(httpRequest), response);
            } else {
                chain.doFilter(request, response);
            }
        }

        private boolean containsNonAscii(String str) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) > 127) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 请求包装器：将查询参数中的非 ASCII 字符重新编码
     */
    public static class EncodedQueryRequestWrapper extends HttpServletRequestWrapper {

        private final String encodedQueryString;

        public EncodedQueryRequestWrapper(HttpServletRequest request) throws UnsupportedEncodingException {
            super(request);
            String original = request.getQueryString();
            if (original == null) {
                this.encodedQueryString = null;
                return;
            }
            // 对查询参数中的每个键值对重新编码
            StringBuilder result = new StringBuilder();
            String[] pairs = original.split("&");
            for (int i = 0; i < pairs.length; i++) {
                if (i > 0) result.append("&");
                int eqIdx = pairs[i].indexOf('=');
                if (eqIdx == -1) {
                    result.append(URLEncoder.encode(pairs[i], StandardCharsets.UTF_8.name()));
                } else {
                    String key = pairs[i].substring(0, eqIdx);
                    String value = pairs[i].substring(eqIdx + 1);
                    // 先解码（防止已经被编码过），再编码
                    String decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8.name());
                    String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                    result.append(URLEncoder.encode(decodedKey, StandardCharsets.UTF_8.name()));
                    result.append("=");
                    result.append(URLEncoder.encode(decodedValue, StandardCharsets.UTF_8.name()));
                }
            }
            this.encodedQueryString = result.toString();
        }

        @Override
        public String getQueryString() {
            return this.encodedQueryString;
        }

        @Override
        public String getRequestURI() {
            // 返回编码后的完整 URI
            String uri = super.getRequestURI();
            if (this.encodedQueryString != null) {
                return uri + "?" + this.encodedQueryString;
            }
            return uri;
        }
    }
}
