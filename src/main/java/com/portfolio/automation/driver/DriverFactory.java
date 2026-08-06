package com.portfolio.automation.driver;

import com.portfolio.automation.config.Configuration;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

/** Creates WebDriver instances while Selenium Manager resolves the required driver binary. */
public final class DriverFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static WebDriver create(String browserName) {
        return create(
                browserName,
                positiveSetting("WINDOW_WIDTH"),
                positiveSetting("WINDOW_HEIGHT"),
                false);
    }

    public static WebDriver create(String browserName, int width, int height) {
        return create(browserName, width, height, false);
    }

    public static WebDriver create(String browserName, int width, int height, boolean emulateViewport) {
        BrowserType browser = BrowserType.from(browserName);
        boolean headless = Configuration.getBoolean("HEADLESS");
        requirePositive("window width", width);
        requirePositive("window height", height);

        LOGGER.info("Creating {} WebDriver (headless={}, window={}x{})", browser, headless, width, height);
        WebDriver driver = switch (browser) {
            case CHROME -> new ChromeDriver(chromeOptions(headless, width, height, emulateViewport));
            case EDGE -> new EdgeDriver(edgeOptions(headless, width, height, emulateViewport));
        };

        configure(driver);
        if (!headless) {
            driver.manage().window().setSize(new Dimension(width, height));
        }
        return driver;
    }

    private static ChromeOptions chromeOptions(
            boolean headless, int width, int height, boolean emulateViewport) {
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.addArguments("--window-size=" + width + "," + height, "--disable-dev-shm-usage");
        if (headless) {
            options.addArguments("--headless=new");
        }
        if (isCi()) {
            options.addArguments("--no-sandbox");
        }
        if (emulateViewport) {
            options.setExperimentalOption("mobileEmulation", viewportEmulation(width, height));
        }
        return options;
    }

    private static EdgeOptions edgeOptions(
            boolean headless, int width, int height, boolean emulateViewport) {
        EdgeOptions options = new EdgeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.addArguments("--window-size=" + width + "," + height, "--disable-dev-shm-usage");
        if (headless) {
            options.addArguments("--headless=new");
        }
        if (isCi()) {
            options.addArguments("--no-sandbox");
        }
        if (emulateViewport) {
            options.setExperimentalOption("mobileEmulation", viewportEmulation(width, height));
        }
        return options;
    }

    private static Map<String, Object> viewportEmulation(int width, int height) {
        return Map.of("deviceMetrics", Map.of(
                "width", width,
                "height", height,
                "pixelRatio", 1.0,
                "mobile", width < 600));
    }

    private static void configure(WebDriver driver) {
        int timeoutSeconds = positiveSetting("SELENIUM_TIMEOUT");
        driver.manage().timeouts()
                .implicitlyWait(Duration.ZERO)
                .scriptTimeout(Duration.ofSeconds(timeoutSeconds))
                .pageLoadTimeout(Duration.ofSeconds(Math.max(30, timeoutSeconds)));
    }

    private static int positiveSetting(String key) {
        int value = Configuration.getInt(key);
        requirePositive(key, value);
        return value;
    }

    private static void requirePositive(String name, int value) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than zero, but was: " + value);
        }
    }

    private static boolean isCi() {
        return "true".equalsIgnoreCase(System.getenv("CI"));
    }
}
