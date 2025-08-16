package com.travelapp.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI travelAppOpenAPI() {
        String jwtSchemeName = "jwtAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
            .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                .name(jwtSchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT 토큰을 사용한 인증. 쿠키에서 자동으로 추출됩니다.")
            );

        return new OpenAPI()
            .addSecurityItem(securityRequirement)
            .components(components)
            .info(new Info()
                .title("Travel Plan API")
                .description("여행 계획 관리 애플리케이션의 REST API 문서입니다.\n\n" +
                    "**인증 방식:**\n" +
                    "- JWT 기반 인증 (Access Token + Refresh Token)\n" +
                    "- 토큰은 쿠키에 자동 저장되며, API 호출 시 자동으로 전송됩니다\n" +
                    "- 대부분의 API는 인증이 필요합니다 (회원가입, 로그인 제외)\n\n" +
                    "**주요 기능:**\n" +
                    "- 회원 관리 (가입, 로그인, 로그아웃)\n" +
                    "- 여행 계획 CRUD\n" +
                    "- 여행 일차별 관리\n" +
                    "- 여행지 관리 및 순서 변경\n" +
                    "- 장소 검색 (카카오 API 연동)")
                .version("v1.0.0")
                .contact(new Contact().name("TravelApp").email("support@example.com"))
                .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"))
            )
            .externalDocs(new ExternalDocumentation()
                .description("GitHub Repository")
                .url("https://github.com/richard3251/travel-plan-app")
            );
    }

}
