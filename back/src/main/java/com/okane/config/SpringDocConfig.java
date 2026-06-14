package com.okane.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.configuration.SpringDocUIConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        SpringDocConfiguration.class,
        SpringDocWebMvcConfiguration.class,
        SpringDocUIConfiguration.class         // ← manquait : charge le Swagger UI
})
public class SpringDocConfig {

    @Bean
    public SpringDocConfigProperties springDocConfigProperties() {
        return new SpringDocConfigProperties();
    }

    // ← manquait : requis par SpringDocUIConfiguration
    @Bean
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties props = new SwaggerUiConfigProperties();
        props.setPath("/swagger-ui.html");      // URL d'accès au Swagger UI
        props.setEnabled(true);
        return props;
    }

    // ← manquait : requis par SpringDocUIConfiguration
    @Bean
    public SwaggerUiOAuthProperties swaggerUiOAuthProperties() {
        return new SwaggerUiOAuthProperties();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Okane API")
                        .version("1.0.0")
                        .description("API de gestion de transferts d'argent")
                        .contact(new Contact()
                                .name("Okane Team")));
    }
}