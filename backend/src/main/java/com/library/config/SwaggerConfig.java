package com.library.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI文档配置
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("图书馆管理系统API")
                        .description("图书馆管理系统后端接口文档，支持图书管理、借阅管理等功能")
                        .version("1.0.0"));
    }
}
