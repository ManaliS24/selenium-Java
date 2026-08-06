package com.portfolio.automation.listeners;

import com.portfolio.automation.driver.DriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class TestListener implements IInvokedMethodListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestListener.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final Path SCREENSHOT_DIRECTORY = Path.of("artifacts", "screenshots");

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        if (method.isTestMethod() && !result.isSuccess()) {
            DriverManager.currentDriver().ifPresent(driver -> captureDiagnostics(result, driver));
        }
    }

    private void captureDiagnostics(ITestResult result, WebDriver driver) {
        try {
            attachText("Current URL", currentUrl(driver));
            attachText("Browser details", browserDetails(driver));

            if (driver instanceof TakesScreenshot screenshotDriver) {
                byte[] png = screenshotDriver.getScreenshotAs(OutputType.BYTES);
                Path destination = screenshotPath(result, browserName(driver));
                Files.createDirectories(SCREENSHOT_DIRECTORY);
                Files.write(destination, png);
                Allure.addAttachment("Failure screenshot", "image/png", new ByteArrayInputStream(png), ".png");
                LOGGER.info("Saved failure screenshot to {}", destination.toAbsolutePath());
            } else {
                LOGGER.warn("WebDriver does not support screenshots: {}", driver.getClass().getName());
            }
        } catch (RuntimeException | IOException diagnosticsFailure) {
            LOGGER.error("Unable to capture diagnostics for {}. The original test failure is preserved.",
                    result.getName(), diagnosticsFailure);
        }
    }

    private static Path screenshotPath(ITestResult result, String browser) {
        String fileName = "%s-%s-%s-thread-%d.png".formatted(
                sanitize(result.getMethod().getQualifiedName()),
                sanitize(browser),
                TIMESTAMP.format(LocalDateTime.now()),
                Thread.currentThread().threadId());
        return SCREENSHOT_DIRECTORY.resolve(fileName);
    }

    private static String currentUrl(WebDriver driver) {
        try {
            return driver.getCurrentUrl();
        } catch (RuntimeException exception) {
            return "Unavailable: " + exception.getClass().getSimpleName();
        }
    }

    private static String browserDetails(WebDriver driver) {
        if (driver instanceof RemoteWebDriver remoteDriver) {
            Capabilities capabilities = remoteDriver.getCapabilities();
            return "browser=%s%nversion=%s%nplatform=%s%nthreadId=%d".formatted(
                    capabilities.getBrowserName(), capabilities.getBrowserVersion(),
                    capabilities.getPlatformName(), Thread.currentThread().threadId());
        }
        return "driver=%s%nthreadId=%d".formatted(
                driver.getClass().getName(), Thread.currentThread().threadId());
    }

    private static String browserName(WebDriver driver) {
        if (driver instanceof RemoteWebDriver remoteDriver) {
            return remoteDriver.getCapabilities().getBrowserName().toLowerCase(Locale.ROOT);
        }
        return driver.getClass().getSimpleName();
    }

    private static void attachText(String name, String value) {
        Allure.addAttachment(name, "text/plain", value, ".txt");
    }

    private static String sanitize(String value) {
        String sanitized = value.replaceAll("[^a-zA-Z0-9._-]", "_");
        return sanitized.length() <= 140 ? sanitized : sanitized.substring(sanitized.length() - 140);
    }
}
