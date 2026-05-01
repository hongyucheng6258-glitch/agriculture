package com.aquaculture.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration with interceptor registration for token authentication (placeholder)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // TODO: Register token authentication interceptor when auth module is implemented
        // registry.addInterceptor(new TokenInterceptor())
        //         .addPathPatterns("/**")
        //         .excludePathPatterns("/api/auth/login", "/api/auth/register");
    }

}
