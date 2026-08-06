package com.portfolio.automation.base;

import com.portfolio.automation.config.Configuration;
import com.portfolio.automation.driver.DriverFactory;
import com.portfolio.automation.driver.DriverManager;
import com.portfolio.automation.listeners.TestListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Dimension;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/** Gives every UI test method a fresh, isolated browser session. */
@Listeners(TestListener.class)
public abstract class BaseUiTest {

    @BeforeMethod(alwaysRun = true)
    @Parameters("browser")
    public void createDriver(@Optional("") String suiteBrowser, Object[] testData) {
        String browser = suiteBrowser == null || suiteBrowser.isBlank()
                ? Configuration.get("BROWSER")
                : suiteBrowser;
        Dimension requestedSize = requestedWindowSize(testData);
        WebDriver driver = DriverFactory.create(
                browser, requestedSize.width, requestedSize.height, emulateViewport(testData));
        try {
            DriverManager.setDriver(driver);
            driver.manage().deleteAllCookies();
        } catch (RuntimeException | Error failure) {
            driver.quit();
            throw failure;
        }
    }

    @AfterMethod(alwaysRun = true)
    public void closeDriver() {
        DriverManager.quitDriver();
    }

    protected WebDriver driver() {
        return DriverManager.getDriver();
    }

    protected Dimension requestedWindowSize(Object[] testData) {
        return new Dimension(
                Configuration.getInt("WINDOW_WIDTH"),
                Configuration.getInt("WINDOW_HEIGHT"));
    }

    protected boolean emulateViewport(Object[] testData) {
        return false;
    }
}
