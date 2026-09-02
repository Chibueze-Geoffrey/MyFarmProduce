package com.myfarmproduce.web.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Accepts the same env var name the previous .NET/Npgsql deployment used -
 * {@code ConnectionStrings__DefaultConnection} - in either format Npgsql
 * accepted: a {@code postgres://user:pass@host:port/db} URI, or the ADO.NET
 * keyword=value form ({@code Host=...;Port=...;Database=...;Username=...;
 * Password=...;SSL Mode=...}). Either is translated into the
 * spring.datasource.* properties Spring Data JPA/Flyway expect, so an
 * existing Render env var keeps working as-is. SPRING_DATASOURCE_URL/
 * _USERNAME/_PASSWORD still work too, as a fallback when this var isn't set.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String LEGACY_VAR = "ConnectionStrings__DefaultConnection";
    private static final String TAG = "[DatabaseUrlEnvironmentPostProcessor]";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String raw = environment.getProperty(LEGACY_VAR);
        if (raw == null || raw.isBlank()) {
            System.out.println(TAG + " " + LEGACY_VAR + " is not set (or blank) - using spring.datasource.* defaults/env vars instead.");
            return;
        }

        Map<String, Object> props = raw.contains("://") ? parseUri(raw) : parseAdoNetKeywords(raw);
        if (props == null || props.isEmpty()) return;

        environment.getPropertySources().addFirst(new MapPropertySource("databaseUrlOverride", props));
        System.out.println(TAG + " Applied datasource from " + LEGACY_VAR + " -> " + props.get("spring.datasource.url")
                + " (username " + (props.containsKey("spring.datasource.username") ? "set" : "NOT set") + ")");
    }

    private Map<String, Object> parseUri(String raw) {
        try {
            URI uri = URI.create(raw);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equals("postgres") || scheme.equals("postgresql"))) {
                System.out.println(TAG + " " + LEGACY_VAR + " looks like a URI but has an unexpected scheme ("
                        + scheme + ") - ignoring it.");
                return null;
            }

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
            return props;
        } catch (IllegalArgumentException e) {
            System.out.println(TAG + " " + LEGACY_VAR + " could not be parsed as a URI (" + e.getMessage() + ") - ignoring it.");
            return null;
        }
    }

    private Map<String, Object> parseAdoNetKeywords(String raw) {
        Map<String, String> kv = new LinkedHashMap<>();
        for (String segment : raw.split(";")) {
            if (segment.isBlank()) continue;
            int eq = segment.indexOf('=');
            if (eq < 0) continue;
            String key = segment.substring(0, eq).trim().toLowerCase(Locale.ROOT).replace(" ", "");
            String value = segment.substring(eq + 1).trim();
            kv.put(key, value);
        }

        String host = kv.get("host");
        String database = kv.get("database");
        if (host == null || database == null) {
            System.out.println(TAG + " " + LEGACY_VAR + " doesn't look like a postgres:// URI or an ADO.NET "
                    + "connection string (missing Host/Database) - ignoring it.");
            return null;
        }

        String port = kv.getOrDefault("port", "5432");
        boolean requireSsl = "require".equalsIgnoreCase(kv.get("sslmode"))
                || "true".equalsIgnoreCase(kv.get("ssl"));

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database
                + (requireSsl ? "?sslmode=require" : "");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("spring.datasource.url", jdbcUrl);
        if (kv.get("username") != null) props.put("spring.datasource.username", kv.get("username"));
        if (kv.get("password") != null) props.put("spring.datasource.password", kv.get("password"));
        return props;
    }
}
