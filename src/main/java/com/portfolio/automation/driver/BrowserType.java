package com.portfolio.automation.driver;

import java.util.Arrays;
import java.util.Locale;

public enum BrowserType {
    CHROME,
    EDGE;

    public static BrowserType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Browser name must not be blank");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported browser '" + value + "'. Supported browsers: "
                            + Arrays.toString(values()),
                    exception);
        }
    }
}
