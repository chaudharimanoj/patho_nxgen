package com.pathonxgen.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pathoNxgenOpenApi() {
        final String bearerScheme = "bearerAuth";
        final String tenantHeaderScheme = "tenantHeader";

        return new OpenAPI()
                .info(new Info()
                        .title("Patho NXGen Platform REST API")
                        .description("Comprehensive multi-tenant SaaS platform API for pathology laboratory operations, sample management, billing, CRM, and analytics.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Patho NXGen Support")
                                .email("support@pathonxgen.com")
                                .url("https://pathonxgen.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://pathonxgen.com/terms")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                .tags(List.of(
                        new Tag().name("Direct Auth").description("Tenant-aware authentication endpoints"),
                        new Tag().name("Tenant Admin").description("Platform tenant management and tenant configurations"),
                        new Tag().name("User Management").description("Tenant user and role management"),
                        new Tag().name("Patient Management").description("Patient registration, search, and medical records"),
                        new Tag().name("Laboratory").description("Sample tracking, testing, and lifecycle state transitions"),
                        new Tag().name("Billing").description("Invoice generation, line items, and payment processing"),
                        new Tag().name("Finance").description("Revenue tracking, expense management, and financial ledgers"),
                        new Tag().name("Inventory").description("Reagent & supply inventory management"),
                        new Tag().name("CRM & Referrals").description("Referral doctor & clinic management"),
                        new Tag().name("Reporting").description("Operational analytics, KPI metrics, and report exports")
                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList(bearerScheme)
                        .addList(tenantHeaderScheme))
                .components(new Components()
                        .addSecuritySchemes(bearerScheme,
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Keycloak OIDC JWT token. Format: `Bearer <JWT_TOKEN>`"))
                        .addSecuritySchemes(tenantHeaderScheme,
                                new SecurityScheme()
                                        .name("X-Tenant-Slug")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .description("Tenant slug identifier (e.g. `demo`, `acme`)")));
    }
}
