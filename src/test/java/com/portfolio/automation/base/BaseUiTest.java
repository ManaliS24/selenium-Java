package com.portfolio.automation.base;

import com.portfolio.automation.config.Configuration;
import com.portfolio.automation.driver.DriverFactory;
import com.portfolio.automation.driver.DriverManager;
import com.portfolio.automation.listeners.TestListener;
import org.openqa.selenium.WebDriver;
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
    public void createDriver(@Optional("") String suiteBrowser) {
        String browser = suiteBrowser == null || suiteBrowser.isBlank()
                ? Configuration.get("BROWSER")
                : suiteBrowser;
        WebDriver driver = DriverFactory.create(browser);
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
}
