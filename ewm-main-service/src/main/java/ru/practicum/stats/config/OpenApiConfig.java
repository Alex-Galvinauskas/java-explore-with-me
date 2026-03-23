package ru.practicum.stats.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Server")
                ))
                .info(new Info()
                        .title("Explore With Me API")
                        .version("1.0.0")
                        .description("API для приложения Explore With Me - поиска и организации событий\n\n" +
                                "## Особенности:\n" +
                                "- Публичный API для просмотра событий и категорий\n" +
                                "- Приватный API для управления собственными событиями\n" +
                                "- Административный API для управления пользователями и контентом\n" +
                                "- Статистика просмотров событий")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
