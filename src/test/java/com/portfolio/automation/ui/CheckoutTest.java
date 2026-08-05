package com.portfolio.automation.ui;

import com.portfolio.automation.base.BaseUiTest;
import com.portfolio.automation.config.Configuration;
import com.portfolio.automation.pages.CartPage;
import com.portfolio.automation.pages.CheckoutPage;
import com.portfolio.automation.pages.InventoryPage;
import com.portfolio.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public final class CheckoutTest extends BaseUiTest {
    private static final String BACKPACK = "Sauce Labs Backpack";

    @Test(groups = {"ui", "smoke"})
    public void customerCanCompleteCheckout() {
        InventoryPage inventory = authenticatedInventory();
        inventory.addProduct(BACKPACK);
        Assert.assertEquals(inventory.cartBadgeQuantity(), "1");

        CartPage cart = inventory.openCart();
        Assert.assertEquals(cart.productNames(), List.of(BACKPACK));

        CheckoutPage checkout = cart.beginCheckout()
                .enterCustomerInformation("Ada", "Lovelace", "85001")
                .continueCheckout()
                .waitForOverview();
        Assert.assertTrue(checkout.total().startsWith("Total: $"), "Checkout total should be displayed");

        checkout.finish();
        Assert.assertEquals(checkout.completionMessage(), "Thank you for your order!");
    }

    @DataProvider(name = "missingCustomerInformation")
    public Object[][] missingCustomerInformation() {
        return new Object[][]{
                {"", "Lovelace", "85001", "First Name is required"},
                {"Ada", "", "85001", "Last Name is required"},
                {"Ada", "Lovelace", "", "Postal Code is required"}
        };
    }

    @Test(dataProvider = "missingCustomerInformation", groups = {"ui", "regression"})
    public void checkoutRequiresCustomerInformation(
            String firstName, String lastName, String postalCode, String expectedMessage) {
        CheckoutPage checkout = authenticatedInventory()
                .addProduct(BACKPACK)
                .openCart()
                .beginCheckout()
                .enterCustomerInformation(firstName, lastName, postalCode)
                .continueCheckout();

        Assert.assertTrue(checkout.errorMessage().contains(expectedMessage),
                "Expected checkout error to contain: " + expectedMessage);
    }

    private InventoryPage authenticatedInventory() {
        return new LoginPage(driver())
                .open()
                .loginAs(Configuration.get("E2E_USERNAME"), Configuration.get("E2E_PASSWORD"))
                .waitUntilLoaded();
    }
}
