package com.camplink.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campLinkOpenAPI() {
        final String scheme = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("CampLink API")
                        .version("1.0.0")
                        .description("""
                                REST API for **CampLink** — a campus marketplace app for Mulungushi University.

                                ## Authentication
                                Most endpoints require a **Bearer JWT token**.
                                1. Call `POST /api/auth/register` or `POST /api/auth/login`.
                                2. Copy the `token` from the response.
                                3. Click **Authorize ↗** and paste the token (without the `Bearer ` prefix — Swagger adds it automatically).

                                ## User roles
                                | Role | Who | Capabilities |
                                |------|-----|--------------|
                                | `BUYER` | Students | Browse products, place orders, chat, leave reviews, post shopping requests |
                                | `SELLER` | Campus vendors | List products, manage incoming orders, chat with buyers |
                                | `RIDER` | Runners / errand runners | Accept and fulfil shopping requests |
                                | `DRIVER` | Transport drivers | Accept ride bookings |
                                | `ADMIN` | Platform admins | Full access — user management, verification, platform settings |

                                ## Public endpoints (no token required)
                                `GET /api/products`, `GET /api/products/{id}`, `GET /api/products/seller/{id}`,
                                `GET /api/reviews/seller/{id}`, `GET /api/reviews/seller/{id}/rating`,
                                `GET /api/requests`, `GET /api/files/{filename}`,
                                `POST /api/auth/register`, `POST /api/auth/login`
                                """)
                        .contact(new Contact()
                                .name("CampLink Support")
                                .email("support@camplink.app")))
                // Global security — every operation requires this unless overridden with security = {}
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components()
                        .addSecuritySchemes(scheme, new SecurityScheme()
                                .name(scheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste your JWT token here (omit the 'Bearer ' prefix).")));
    }
}
