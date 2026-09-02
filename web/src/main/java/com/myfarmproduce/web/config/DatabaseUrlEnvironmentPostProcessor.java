package com.myfarmproduce.web.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Accepts a single Postgres URI connection string under the same env var name
 * the previous .NET/Npgsql deployment used - {@code ConnectionStrings__DefaultConnection},
 * e.g. {@code postgres://user:pass@host:5432/db} - and translates it into the
 * spring.datasource.* properties Spring Data JPA/Flyway expect. This lets an
 * existing Render env var be reused as-is instead of splitting it into three
 * (SPRING_DATASOURCE_URL/_USERNAME/_PASSWORD, which still work as a fallback
 * if this variable isn't set).
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String LEGACY_VAR = "ConnectionStrings__DefaultConnection";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String raw = environment.getProperty(LEGACY_VAR);
        if (raw == null || raw.isBlank()) return;

        try {
            URI uri = URI.create(raw);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equals("postgres") || scheme.equals("postgresql"))) return;

            String username = null;
            String password = null;
            String userInfo = uri.getUserInfo();
            if (userInfo != null) {
                String[] parts = userInfo.split(":", 2);
                username = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                if (parts.length > 1) password = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }

            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String database = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
            String query = uri.getRawQuery();

            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + "/" + database
                    + (query != null && !query.isBlank() ? "?" + query : "");

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            if (username != null) props.put("spring.datasource.username", username);
            if (password != null) props.put("spring.datasource.password", password);

            environment.getPropertySources().addFirst(new MapPropertySource("databaseUrlOverride", props));
        } catch (IllegalArgumentException ignored) {
            // Not a parseable URI - leave spring.datasource.* to be resolved some other way.
        }
    }
}
