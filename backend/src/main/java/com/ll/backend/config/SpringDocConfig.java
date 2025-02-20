package com.ll.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "API 서버", version = "v1"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SpringDocConfig {

    @Bean
    public GroupedOpenApi groupAdmin() {
        return GroupedOpenApi.builder()
                .group("ADMIN")
                .pathsToMatch("/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupJoin() {
        return GroupedOpenApi.builder()
                .group("JOIN")
                .pathsToMatch("/join/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupJwt() {
        return GroupedOpenApi.builder()
                .group("JWT")
                .pathsToMatch("/cookie-to-header")
                .build();
    }

    @Bean
    public GroupedOpenApi groupMain() {
        return GroupedOpenApi.builder()
                .group("MAIN")
                .pathsToMatch("/")
                .build();
    }

    @Bean
    public GroupedOpenApi groupMy() {
        return GroupedOpenApi.builder()
                .group("MY")
                .pathsToMatch("/my/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupLogin() {
        return GroupedOpenApi.builder()
                .group("LOGIN")
                .pathsToMatch("/login")
                .addOpenApiCustomizer(openApi -> openApi.getPaths().addPathItem("/login", createLoginPathItem()))
                .build();
    }

    private PathItem createLoginPathItem() {
        return new PathItem()
                .post(new Operation()
                        .addTagsItem("인증")
                        .summary("일반 로그인")
                        .requestBody(new RequestBody()
                                .content(new Content()
                                        .addMediaType("multipart/form-data", new MediaType()
                                                .schema(new Schema<>()
                                                        .type("object")
                                                        .addProperties("username", new Schema<>().type("string"))
                                                        .addProperties("password", new Schema<>().type("string"))))))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("로그인 성공. 발급된 토큰은 Response headers의 Authorization 부분을 확인해주세요."))
                                .addApiResponse("401", new ApiResponse().description("로그인 실패"))));
    }
}