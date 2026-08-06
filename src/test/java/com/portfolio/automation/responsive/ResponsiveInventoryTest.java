package com.portfolio.automation.responsive;

import com.portfolio.automation.base.BaseUiTest;
import com.portfolio.automation.config.Configuration;
import com.portfolio.automation.pages.CartPage;
import com.portfolio.automation.pages.InventoryPage;
import com.portfolio.automation.pages.LoginPage;
import org.openqa.selenium.Dimension;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class ResponsiveInventoryTest extends BaseUiTest {
    private static final String BACKPACK = "Sauce Labs Backpack";
    private static final int VIEWPORT_TOLERANCE = 2;

    @DataProvider(name = "viewports")
    public Object[][] viewports() {
        return new Object[][]{
                {"desktop", 1_440, 900},
                {"mobile", 390, 844}
        };
    }

    @Test(dataProvider = "viewports", groups = {"ui", "responsive", "regression"})
    public void inventoryAdaptsWithoutHorizontalOverflow(String name, int width, int height) {
        InventoryPage inventory = new LoginPage(driver())
                .open()
                .loginAs(Configuration.get("E2E_USERNAME"), Configuration.get("E2E_PASSWORD"))
                .waitUntilLoaded();

        Assert.assertTrue(inventory.currentUrl().endsWith("/inventory.html"));
        Assert.assertEquals(inventory.title(), "Products");
        Assert.assertTrue(inventory.isProductVisible(BACKPACK));
        Assert.assertTrue(inventory.isCartVisible());

        InventoryPage.LayoutMetrics layout = inventory.layoutMetrics();
        Assert.assertTrue(layout.scrollWidth() <= layout.clientWidth(),
                name + " layout has horizontal overflow: " + layout);
        Assert.assertTrue(Math.abs(layout.innerWidth() - width) <= VIEWPORT_TOLERANCE,
                name + " viewport width was not applied: " + layout.innerWidth());
        Assert.assertTrue(Math.abs(layout.innerHeight() - height) <= VIEWPORT_TOLERANCE,
                name + " viewport height was not applied: " + layout.innerHeight());

        CartPage cart = inventory.openCart();
        Assert.assertTrue(driver().getCurrentUrl().endsWith("/cart.html"));
        Assert.assertNotNull(cart);
    }

    @Override
    protected Dimension requestedWindowSize(Object[] testData) {
        if (testData.length >= 3 && testData[1] instanceof Integer width
                && testData[2] instanceof Integer height) {
            return new Dimension(width, height);
        }
        throw new IllegalArgumentException("Responsive test data must provide viewport width and height");
    }

    @Override
    protected boolean emulateViewport(Object[] testData) {
        return true;
    }
}
