package com.portfolio.automation.pages;

import com.portfolio.automation.config.Configuration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Explicit-wait browser operations shared by concrete page objects. */
public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = Objects.requireNonNull(driver, "WebDriver must not be null");
        int timeoutSeconds = Configuration.getInt("SELENIUM_TIMEOUT");
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("SELENIUM_TIMEOUT must be greater than zero");
        }
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected List<WebElement> visibleElements(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected WebElement find(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected void click(By locator) {
        clickable(locator).click();
    }

    protected void type(By locator, String value) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(value == null ? "" : value);
    }

    protected String text(By locator) {
        return visible(locator).getText();
    }

    protected void waitForUrlContaining(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    protected void waitForUrl(Predicate<String> condition) {
        wait.until(webDriver -> condition.test(webDriver.getCurrentUrl()));
    }

    protected Object executeScript(String script, Object... arguments) {
        if (!(driver instanceof JavascriptExecutor executor)) {
            throw new IllegalStateException("The current WebDriver does not support JavaScript execution");
        }
        return executor.executeScript(script, arguments);
    }
}
