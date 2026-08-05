package com.portfolio.automation.driver;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class BrowserTypeTest {

    @DataProvider
    public Object[][] supportedBrowsers() {
        return new Object[][]{
                {"chrome", BrowserType.CHROME},
                {"CHROME", BrowserType.CHROME},
                {" edge ", BrowserType.EDGE}
        };
    }

    @Test(dataProvider = "supportedBrowsers")
    public void parsesSupportedBrowserNames(String value, BrowserType expected) {
        Assert.assertEquals(BrowserType.from(value), expected);
    }

    @Test
    public void rejectsUnsupportedBrowserImmediately() {
        IllegalArgumentException exception = Assert.expectThrows(
                IllegalArgumentException.class, () -> BrowserType.from("firefox"));

        Assert.assertTrue(exception.getMessage().contains("Unsupported browser 'firefox'"));
        Assert.assertTrue(exception.getMessage().contains("CHROME"));
        Assert.assertTrue(exception.getMessage().contains("EDGE"));
    }
}
