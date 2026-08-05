package com.portfolio.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/** Resolves settings from system properties, environment variables, properties, then defaults. */
public final class Configuration {
    private static final String RESOURCE_NAME = "config.properties";
    private static final Properties FILE_VALUES = loadProperties();
    private static final Map<String, String> DEFAULTS = Map.ofEntries(
            Map.entry("UI_BASE_URL", "https://www.saucedemo.com"),
            Map.entry("API_BASE_URL", "https://jsonplaceholder.typicode.com"),
            Map.entry("E2E_USERNAME", "standard_user"),
            Map.entry("E2E_PASSWORD", "secret_sauce"),
            Map.entry("DB_HOST", "relational.fel.cvut.cz"),
            Map.entry("DB_PORT", "3306"),
            Map.entry("DB_NAME", "classicmodels"),
            Map.entry("DB_USER", "guest"),
            Map.entry("DB_PASSWORD", "ctu-relational"),
            Map.entry("SELENIUM_TIMEOUT", "10"),
            Map.entry("HEADLESS", "true"),
            Map.entry("BROWSER", "chrome"),
            Map.entry("WINDOW_WIDTH", "1440"),
            Map.entry("WINDOW_HEIGHT", "900")
    );

    private Configuration() {
    }

    public static String get(String key) {
        String normalizedKey = key.toUpperCase(Locale.ROOT);
        String systemValue = firstNonBlank(
                System.getProperty(toSystemProperty(normalizedKey)),
                System.getProperty(normalizedKey));
        if (systemValue != null) {
            return systemValue;
        }

        String environmentValue = System.getenv(normalizedKey);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        String fileValue = FILE_VALUES.getProperty(normalizedKey);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue.trim();
        }

        String defaultValue = DEFAULTS.get(normalizedKey);
        if (defaultValue == null) {
            throw new IllegalArgumentException("Unknown configuration key: " + key);
        }
        return defaultValue;
    }

    public static int getInt(String key) {
        String value = get(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " must be an integer, but was: " + value, exception);
        }
    }

    public static boolean getBoolean(String key) {
        String value = get(key);
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException(key + " must be true or false, but was: " + value);
        }
        return Boolean.parseBoolean(value);
    }

    private static String toSystemProperty(String key) {
        return switch (key) {
            case "UI_BASE_URL" -> "uiBaseUrl";
            case "API_BASE_URL" -> "apiBaseUrl";
            case "E2E_USERNAME" -> "username";
            case "E2E_PASSWORD" -> "password";
            case "DB_HOST" -> "dbHost";
            case "DB_PORT" -> "dbPort";
            case "DB_NAME" -> "dbName";
            case "DB_USER" -> "dbUser";
            case "DB_PASSWORD" -> "dbPassword";
            case "SELENIUM_TIMEOUT" -> "timeout";
            case "HEADLESS" -> "headless";
            case "BROWSER" -> "browser";
            case "WINDOW_WIDTH" -> "windowWidth";
            case "WINDOW_HEIGHT" -> "windowHeight";
            default -> key.toLowerCase(Locale.ROOT);
        };
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream stream = Configuration.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
            if (stream != null) {
                properties.load(stream);
            }
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load " + RESOURCE_NAME, exception);
        }
    }
}
