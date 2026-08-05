package com.portfolio.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class CheckoutPage extends BasePage {
    private static final By FIRST_NAME = By.cssSelector("[data-test='firstName']");
    private static final By LAST_NAME = By.cssSelector("[data-test='lastName']");
    private static final By POSTAL_CODE = By.cssSelector("[data-test='postalCode']");
    private static final By CONTINUE_BUTTON = By.cssSelector("[data-test='continue']");
    private static final By ERROR_MESSAGE = By.cssSelector("[data-test='error']");
    private static final By TOTAL = By.cssSelector("[data-test='total-label']");
    private static final By FINISH_BUTTON = By.cssSelector("[data-test='finish']");
    private static final By COMPLETE_HEADER = By.cssSelector("[data-test='complete-header']");

    public CheckoutPage(WebDriver driver) {
        super(driver);
    }

    public CheckoutPage waitForInformationStep() {
        waitForUrlContaining("/checkout-step-one.html");
        visible(CONTINUE_BUTTON);
        return this;
    }

    public CheckoutPage enterCustomerInformation(String firstName, String lastName, String postalCode) {
        type(FIRST_NAME, firstName);
        type(LAST_NAME, lastName);
        type(POSTAL_CODE, postalCode);
        return this;
    }

    public CheckoutPage continueCheckout() {
        click(CONTINUE_BUTTON);
        return this;
    }

    public CheckoutPage waitForOverview() {
        waitForUrlContaining("/checkout-step-two.html");
        visible(TOTAL);
        return this;
    }

    public String total() {
        return text(TOTAL);
    }

    public CheckoutPage finish() {
        click(FINISH_BUTTON);
        waitForUrlContaining("/checkout-complete.html");
        return this;
    }

    public String completionMessage() {
        return text(COMPLETE_HEADER);
    }

    public String errorMessage() {
        return text(ERROR_MESSAGE);
    }
}
