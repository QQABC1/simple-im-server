package com.shixun.simpleimserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-path}")
    private String accessPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射逻辑：/files/** -> 本地磁盘路径
        // 注意：本地路径前必须加 "file:" 前缀
        registry.addResourceHandler(accessPath)
                .addResourceLocations("file:" + uploadPath);
    }
}