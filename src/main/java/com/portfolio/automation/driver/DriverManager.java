package com.portfolio.automation.driver;

import org.openqa.selenium.WebDriver;

import java.util.Optional;

/** Owns exactly one WebDriver per executing test thread. */
public final class DriverManager {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void setDriver(WebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver must not be null");
        }
        if (DRIVER.get() != null) {
            throw new IllegalStateException("A WebDriver is already registered for this thread");
        }
        DRIVER.set(driver);
    }

    public static WebDriver getDriver() {
        return currentDriver().orElseThrow(
                () -> new IllegalStateException("No WebDriver is registered for the current thread"));
    }

    public static Optional<WebDriver> currentDriver() {
        return Optional.ofNullable(DRIVER.get());
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        try {
            if (driver != null) {
                driver.quit();
            }
        } finally {
            DRIVER.remove();
        }
    }
}
