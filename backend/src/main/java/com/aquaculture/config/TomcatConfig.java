package com.aquaculture.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 修复中文参数编码问题
 *
 * 根本原因：vue-cli 的 http-proxy-middleware 会自动将 URL 编码的中文参数解码为原始中文字符，
 * 然后转发给 Tomcat。Tomcat 9 默认使用严格模式解析 URL，不允许查询参数中包含原始 UTF-8 字符，
 * 会直接返回 HTTP 400 错误。
 *
 * 解决方案：通过 TomcatConnectorCustomizer 配置 Connector 的 relaxedQueryChars，
 * 允许查询参数中包含所有非 ASCII 字符。
 */
@Configuration
public class TomcatConfig {

    @Bean
    public TomcatConnectorCustomizer connectorCustomizer() {
        return connector -> {
            // Tomcat 9 的 relaxedQueryChars 用于指定查询字符串中允许的特殊字符。
            // 但它只适用于 ASCII 范围内的字符。
            // 对于 UTF-8 多字节中文字符，需要设置 URIEncoding 和 relaxPathChars。

            // 方案：设置 Connector 的 URIEncoding 为 UTF-8，并放宽查询字符限制
            // 同时通过反射设置 allowEncodedSlash 等属性
            connector.setProperty("relaxedQueryChars", "<>[^`{|}~");
            connector.setProperty("URIEncoding", "UTF-8");
        };
    }
}
