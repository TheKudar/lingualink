package com.lingualink.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI lingualinkOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("LinguaLink API")
                        .version("v1")
                        .description("REST API for authentication, users, courses, reading materials, and chat."))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT access token returned by register or login.")))
                .addTagsItem(new Tag().name("Authentication").description("Registration, login, and current identity"))
                .addTagsItem(new Tag().name("Users").description("User profiles, avatars, search, and admin management"))
                .addTagsItem(new Tag().name("Courses").description("Course catalog, enrollment, progress, moderation, and reviews"))
                .addTagsItem(new Tag().name("Modules").description("Course module management"))
                .addTagsItem(new Tag().name("Lessons").description("Lesson management and completion tracking"))
                .addTagsItem(new Tag().name("Exercises").description("Exercise management and answer submission"))
                .addTagsItem(new Tag().name("Reading Materials").description("Standalone reading practice materials"))
                .addTagsItem(new Tag().name("Chat").description("Conversations and messages"))
                .addTagsItem(new Tag().name("Health").description("Service health check"));
    }
}
