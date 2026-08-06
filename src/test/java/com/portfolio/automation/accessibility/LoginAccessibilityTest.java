package com.portfolio.automation.accessibility;

import com.deque.html.axecore.results.CheckedNode;
import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.automation.base.BaseUiTest;
import com.portfolio.automation.pages.LoginPage;
import io.qameta.allure.Allure;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

public final class LoginAccessibilityTest extends BaseUiTest {
    private static final List<String> WCAG_TAGS = List.of(
            "wcag2a", "wcag2aa", "wcag21a", "wcag21aa");

    @Test(groups = {"ui", "accessibility", "regression"})
    public void loginPageHasNoUnapprovedWcagViolations() {
        new LoginPage(driver()).open();

        Results results = new AxeBuilder()
                .withTags(WCAG_TAGS)
                .setTimeout(30_000)
                .analyze(driver());

        Assert.assertFalse(results.isErrored(), "axe scan failed: " + results.getErrorMessage());
        attachResults(results);
        List<Rule> violations = results.getViolations();
        Assert.assertTrue(violations.isEmpty(), formatViolations(violations));
    }

    private static String formatViolations(List<Rule> violations) {
        if (violations.isEmpty()) {
            return "No accessibility violations";
        }
        return violations.stream().map(rule -> """
                Rule: %s
                Impact: %s
                Help: %s
                Affected nodes: %d
                Selectors: %s
                """.formatted(
                rule.getId(),
                rule.getImpact(),
                rule.getHelp(),
                rule.getNodes().size(),
                rule.getNodes().stream()
                        .map(CheckedNode::getTarget)
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "))))
                .collect(Collectors.joining("\n"));
    }

    private static void attachResults(Results results) {
        try {
            String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(results);
            Allure.addAttachment("axe accessibility results", "application/json", json, ".json");
        } catch (JsonProcessingException exception) {
            Allure.addAttachment("axe accessibility summary", "text/plain",
                    formatViolations(results.getViolations()), ".txt");
        }
    }
}
