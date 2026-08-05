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

/** Creates WebDriver instances while Selenium Manager resolves the required driver binary. */
public final class DriverFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static WebDriver create(String browserName) {
        BrowserType browser = BrowserType.from(browserName);
        boolean headless = Configuration.getBoolean("HEADLESS");
        int width = positiveSetting("WINDOW_WIDTH");
        int height = positiveSetting("WINDOW_HEIGHT");

        LOGGER.info("Creating {} WebDriver (headless={}, window={}x{})", browser, headless, width, height);
        WebDriver driver = switch (browser) {
            case CHROME -> new ChromeDriver(chromeOptions(headless, width, height));
            case EDGE -> new EdgeDriver(edgeOptions(headless, width, height));
        };

        configure(driver);
        if (!headless) {
            driver.manage().window().setSize(new Dimension(width, height));
        }
        return driver;
    }

    private static ChromeOptions chromeOptions(boolean headless, int width, int height) {
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.addArguments("--window-size=" + width + "," + height, "--disable-dev-shm-usage");
        if (headless) {
            options.addArguments("--headless=new");
        }
        if (isCi()) {
            options.addArguments("--no-sandbox");
        }
        return options;
    }

    private static EdgeOptions edgeOptions(boolean headless, int width, int height) {
        EdgeOptions options = new EdgeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        options.addArguments("--window-size=" + width + "," + height, "--disable-dev-shm-usage");
        if (headless) {
            options.addArguments("--headless=new");
        }
        if (isCi()) {
            options.addArguments("--no-sandbox");
        }
        return options;
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
        if (value <= 0) {
            throw new IllegalArgumentException(key + " must be greater than zero, but was: " + value);
        }
        return value;
    }

    private static boolean isCi() {
        return "true".equalsIgnoreCase(System.getenv("CI"));
    }
}
