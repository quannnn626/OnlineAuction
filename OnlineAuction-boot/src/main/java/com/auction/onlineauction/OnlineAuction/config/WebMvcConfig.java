package com.auction.onlineauction.OnlineAuction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 配置跨域支持（CORS）和静态资源映射
 *
 * @author MrYan
 * @since 2025-12-31
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 统一配置：所有 /api/** 路径都允许跨域
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")  // 允许所有来源（生产环境应指定具体域名）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Set-Cookie")  // 暴露Set-Cookie头，允许前端读取
                .allowCredentials(true)  // 允许携带Cookie
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，将 /upload/** 路径映射到项目根目录下的 upload 文件夹
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/upload/");
    }
}

