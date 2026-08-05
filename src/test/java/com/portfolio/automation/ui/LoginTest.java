package com.portfolio.automation.ui;

import com.portfolio.automation.base.BaseUiTest;
import com.portfolio.automation.config.Configuration;
import com.portfolio.automation.pages.InventoryPage;
import com.portfolio.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class LoginTest extends BaseUiTest {

    @Test(groups = {"ui", "smoke"})
    public void standardUserCanLogIn() {
        InventoryPage inventory = new LoginPage(driver())
                .open()
                .loginAs(Configuration.get("E2E_USERNAME"), Configuration.get("E2E_PASSWORD"))
                .waitUntilLoaded();

        Assert.assertTrue(inventory.currentUrl().endsWith("/inventory.html"));
        Assert.assertEquals(inventory.title(), "Products");
    }

    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentials() {
        return new Object[][]{
                {"locked_out_user", "secret_sauce", "locked out"},
                {"standard_user", "wrong_password", "Username and password do not match"},
                {"", "", "Username is required"}
        };
    }

    @Test(dataProvider = "invalidCredentials", groups = {"ui", "regression"})
    public void invalidLoginShowsUsefulError(String username, String password, String expectedMessage) {
        LoginPage login = new LoginPage(driver()).open();
        login.loginAs(username, password);

        Assert.assertTrue(login.errorMessage().contains(expectedMessage),
                "Expected login error to contain: " + expectedMessage);
    }
}
