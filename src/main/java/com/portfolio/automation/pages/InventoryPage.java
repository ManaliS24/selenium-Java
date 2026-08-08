package com.portfolio.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Map;

public final class InventoryPage extends BasePage {
    private static final By PAGE_TITLE = By.cssSelector("[data-test='title']");
    private static final By INVENTORY_ITEMS = By.cssSelector("[data-test='inventory-item']");
    private static final By ITEM_NAME = By.cssSelector("[data-test='inventory-item-name']");
    private static final By ADD_TO_CART = By.cssSelector("button[data-test^='add-to-cart']");
    private static final By CART_LINK = By.cssSelector("[data-test='shopping-cart-link']");
    private static final By CART_BADGE = By.cssSelector("[data-test='shopping-cart-badge']");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public InventoryPage waitUntilLoaded() {
        waitForUrlContaining("/inventory.html");
        visible(PAGE_TITLE);
        return this;
    }

    public String title() {
        return text(PAGE_TITLE);
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean isProductVisible(String productName) {
        return product(productName).findElement(ITEM_NAME).isDisplayed();
    }

    public InventoryPage addProduct(String productName) {
        WebElement addButton = product(productName).findElement(ADD_TO_CART);
        clickUntil(addButton, webDriver -> isCartBadgeVisible());
        return this;
    }

    public String cartBadgeQuantity() {
        return text(CART_BADGE);
    }

    public boolean isCartVisible() {
        return visible(CART_LINK).isDisplayed();
    }

    public CartPage openCart() {
        clickUntil(CART_LINK, webDriver -> webDriver.getCurrentUrl().contains("/cart.html"));
        return new CartPage(driver).waitUntilLoaded();
    }

    private boolean isCartBadgeVisible() {
        return driver.findElements(CART_BADGE).stream().anyMatch(WebElement::isDisplayed);
    }

    @SuppressWarnings("unchecked")
    public LayoutMetrics layoutMetrics() {
        Map<String, Number> measurements = (Map<String, Number>) executeScript("""
                return {
                    clientWidth: document.documentElement.clientWidth,
                    scrollWidth: document.documentElement.scrollWidth,
                    innerWidth: window.innerWidth,
                    innerHeight: window.innerHeight
                };
                """);
        return new LayoutMetrics(
                measurements.get("clientWidth").intValue(),
                measurements.get("scrollWidth").intValue(),
                measurements.get("innerWidth").intValue(),
                measurements.get("innerHeight").intValue());
    }

    private WebElement product(String productName) {
        return visibleElements(INVENTORY_ITEMS).stream()
                .filter(item -> productName.equals(item.findElement(ITEM_NAME).getText()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Inventory product not found: " + productName));
    }

    public record LayoutMetrics(int clientWidth, int scrollWidth, int innerWidth, int innerHeight) {
    }
}
