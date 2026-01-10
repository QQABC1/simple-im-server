package com.shixun.simpleimserver.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableOpenApi // 开启 Swagger3
@EnableKnife4j   // 开启 Knife4j 增强功能
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30) // 使用 OpenAPI 3.0
                .apiInfo(apiInfo())
                .select()
                // 扫描 Controller 所在的包路径 (请修改为你自己的包名)
                .apis(RequestHandlerSelectors.basePackage("com.shixun.simpleimserver.controller"))
                .paths(PathSelectors.any())
                .build()
                // 添加登录认证 (Authorize 按钮)
                .securitySchemes(Collections.singletonList(securityScheme()))
                .securityContexts(Collections.singletonList(securityContext()));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("简易即时通讯系统 API文档")
                .description("Simple IM Server 接口文档")
                .version("1.0")
                .build();
    }

    // =====================================
    // 下面的配置是为了在 Swagger 页面上添加 "Authorize" 按钮
    // =====================================

    /**
     * 配置认证模式 (JWT)
     */
    private SecurityScheme securityScheme() {
        // "Authorization" 是请求头中的 Key，"header" 表示参数在 Header 中
        return new ApiKey("Authorization", "Authorization", "header");
    }

    /**
     * 配置认证上下文
     */
    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .operationSelector(o -> !o.requestMappingPattern().matches("/api/auth/.*"))
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(new SecurityReference("Authorization", authorizationScopes));
    }
}