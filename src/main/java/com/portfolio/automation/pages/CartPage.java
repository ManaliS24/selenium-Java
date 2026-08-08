package com.portfolio.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.List;

public final class CartPage extends BasePage {
    private static final By CART_ITEM_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By CHECKOUT_BUTTON = By.cssSelector("[data-test='checkout']");

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public CartPage waitUntilLoaded() {
        waitForUrlContaining("/cart.html");
        visible(CHECKOUT_BUTTON);
        return this;
    }

    public List<String> productNames() {
        return visibleElements(CART_ITEM_NAMES).stream().map(element -> element.getText()).toList();
    }

    public CheckoutPage beginCheckout() {
        clickUntil(CHECKOUT_BUTTON,
                webDriver -> webDriver.getCurrentUrl().contains("/checkout-step-one.html"));
        return new CheckoutPage(driver).waitForInformationStep();
    }
}
