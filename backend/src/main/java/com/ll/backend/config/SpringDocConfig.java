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
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

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
    public GroupedOpenApi groupAll() {
        return GroupedOpenApi.builder()
                .group("ALL")
                .pathsToMatch("/**")
                .addOpenApiCustomizer(openApi -> {
                    openApi.getPaths()
                            .addPathItem("/login", createBasicLoginPathItem())
                            .addPathItem("/oauth2/authorization/google", createSocialLoginPathItem("Google"))
                            .addPathItem("/oauth2/authorization/naver", createSocialLoginPathItem("Naver"))
                            .addPathItem("/oauth2/authorization/kakao", createSocialLoginPathItem("Kakao"));
                })
                .build();
    }

    private PathItem createBasicLoginPathItem() {
        return new PathItem()
                .post(new Operation()
                        .addTagsItem("인증")
                        .summary("일반 로그인")
                        .requestBody(new RequestBody()
                                .content(new Content()
                                        .addMediaType("application/x-www-form-urlencoded", new MediaType()
                                                .schema(new Schema<>()
                                                        .type("object")
                                                        .addProperties("username", new Schema<>().type("string"))
                                                        .addProperties("password", new Schema<>().type("string"))))))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("로그인 성공. JWT 토큰이 Authorization 헤더에 반환됩니다."))
                                .addApiResponse("401", new ApiResponse()
                                        .description("로그인 실패"))));
    }

    private PathItem createSocialLoginPathItem(String provider) {
        return new PathItem()
                .get(new Operation()
                        .addTagsItem("소셜 로그인")
                        .summary(provider + " 로그인")
                        .description("※ Swagger에서 직접 테스트는 불가능합니다. 브라우저에서 직접 접근해주세요.\n" +
                                     "실제 호출 URL: /oauth2/authorization/" + provider.toLowerCase())
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("소셜 로그인 성공 시 JWT 토큰 발급"))
                                .addApiResponse("302", new ApiResponse()
                                        .description(provider + " 로그인 페이지로 리다이렉트")))
                        .security(Arrays.asList(
                                new SecurityRequirement().addList("oauth2")
                        )));
    }
}