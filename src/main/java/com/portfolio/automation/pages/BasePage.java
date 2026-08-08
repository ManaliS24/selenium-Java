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

    protected void clickUntil(By locator, Predicate<WebDriver> expectedState) {
        boolean[] nativeClickAttempted = {false};
        wait.until(webDriver -> {
            if (expectedState.test(webDriver)) {
                return true;
            }
            WebElement element = clickable(locator);
            if (nativeClickAttempted[0]) {
                executeScript("arguments[0].click();", element);
            } else {
                element.click();
                nativeClickAttempted[0] = true;
            }
            return expectedState.test(webDriver);
        });
    }

    protected void clickUntil(WebElement element, Predicate<WebDriver> expectedState) {
        boolean[] nativeClickAttempted = {false};
        wait.until(webDriver -> {
            if (expectedState.test(webDriver)) {
                return true;
            }
            if (nativeClickAttempted[0]) {
                executeScript("arguments[0].click();", element);
            } else {
                element.click();
                nativeClickAttempted[0] = true;
            }
            return expectedState.test(webDriver);
        });
    }

    protected void type(By locator, String value) {
        WebElement element = visible(locator);
        String expectedValue = value == null ? "" : value;
        element.clear();
        element.sendKeys(expectedValue);
        wait.until(webDriver -> {
            if (expectedValue.equals(element.getAttribute("value"))) {
                return true;
            }
            executeScript("""
                    const input = arguments[0];
                    const value = arguments[1];
                    const setter = Object.getOwnPropertyDescriptor(
                        window.HTMLInputElement.prototype, 'value').set;
                    setter.call(input, value);
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                    input.dispatchEvent(new Event('change', { bubbles: true }));
                    """, element, expectedValue);
            return expectedValue.equals(element.getAttribute("value"));
        });
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
