package org.pado.api.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Collections;

@Configuration
@Profile("!prod") // 운영환경에서 비활성화  
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        // JWT 토큰을 위한 보안 스키마 이름 정의
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("PADO API Docs")
                        .version("v3")
                        .description("PADO 프로젝트용 Swagger 문서입니다.")
                )
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }

//     /**
//      * 관리자용 OpenAPI 설정 (인증 불필요), 드롭 다운 형식
//      */
//     @Bean("adminOpenAPI")
//     @Primary
//     public OpenAPI adminOpenAPI() {
//         return new OpenAPI()
//                 .info(new Info()
//                         .title("PADO ADMIN API Docs")
//                         .version("v1")
//                         .description("PADO 관리자용 API 문서입니다.(인증 불필요)")
//                 );
//     }

    // 드롭다운
    /**
     * 일반 사용자용 API 그룹(인증 필요)
     */
    @Bean
    public GroupedOpenApi userApi() {
         return GroupedOpenApi.builder()
                .group("2-user") // 숫자 붙여서 순서 제어
                .displayName("User API (Auth Required)")
                .pathsToMatch("/signin", "/signup", "/signout", "/finduser", "/passwordreset",
                                "/users/**",
                                "/credentials/**",
                                "/projects/**",
                                "/components/**",
                                "/search/**", "/status", "/monitoring", "/guidelines")
                .build();
    }

    /**
     * 관리자용 API 그룹(인증 불필요)
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("1-admin") // 숫자 붙여서 순서 제어
                .displayName("admin API (No Auth Required)")
                .pathsToMatch("/admin-test/**")
                .addOpenApiCustomizer(openApi -> openApi
                    .security(Collections.emptyList())) // SecurityRequirement 제거
                .build();
    }
}

