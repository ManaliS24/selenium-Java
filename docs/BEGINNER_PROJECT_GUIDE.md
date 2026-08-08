# Beginner Guide to This Java Automation Framework

This guide explains how the project is organized, how a test reaches a system under
test, and how to add coverage without mixing responsibilities.

## 1. The testing layers

The Maven project uses TestNG as its single test runner:

- Selenium UI tests drive SauceDemo in Chrome or Edge.
- REST Assured tests call JSONPlaceholder directly.
- JDBC tests validate a fresh SQLite database or the external ClassicModels dataset.
- axe-core scans a rendered page for common accessibility problems.
- Responsive tests render inventory at desktop and mobile viewport sizes.

Only UI, responsive, and accessibility tests create a browser.

## 2. Follow one UI test

For `LoginTest.standardUserCanLogIn`, the flow is:

```text
TestNG
  -> BaseUiTest creates a fresh WebDriver
  -> DriverFactory selects Chrome or Edge
  -> Selenium Manager resolves the driver executable
  -> LoginPage opens SauceDemo and enters credentials
  -> InventoryPage waits for the destination
  -> LoginTest asserts the URL and title
  -> BaseUiTest always quits the driver
```

`DriverManager` stores the driver in `ThreadLocal`, so parallel tests do not share a
browser. `BasePage` supplies explicit waits and common interactions. Concrete page
objects keep their own `data-test` selectors.

## 3. Why assertions stay in tests

A page object describes how to interact with a page. A test describes expected business
behavior. For example, `CheckoutPage.total()` returns visible text; `CheckoutTest`
asserts that it begins with `Total: $`. This separation makes page objects reusable and
keeps failures readable.

## 4. Configuration

`Configuration` checks, in order:

1. A Java system property
2. An environment variable
3. `config.properties`
4. A built-in default

This command overrides the browser and visibility without changing source code:

```powershell
.\mvnw.cmd test -Dgroups=ui -Dbrowser=edge -Dheadless=false
```

Use environment variables or CI secrets for values that must not be committed. The
checked-in `.env.example` is documentation; export its values in your shell when needed.

## 5. Add a page object

Extend `BasePage`, keep selectors private, and expose business-friendly operations:

```java
public final class ExamplePage extends BasePage {
    private static final By SAVE = By.cssSelector("[data-test='save']");

    public ExamplePage(WebDriver driver) {
        super(driver);
    }

    public ExamplePage save() {
        click(SAVE);
        return this;
    }
}
```

Prefer `data-test`, then ID/name, accessible attributes, stable CSS, and only use XPath
when no clearer stable selector exists. Never add fixed delays; add an explicit wait for
the state the user actually needs.

## 6. Add a UI test

Extend `BaseUiTest`, assign groups, use page objects, and keep assertions in the test:

```java
@Test(groups = {"ui", "smoke"})
public void userCanSave() {
    ExamplePage page = new ExamplePage(driver());
    page.save();
    Assert.assertTrue(/* observable result */);
}
```

Do not cache login or cart state across tests. A clean browser per test is intentionally
favored over premature optimization.

## 7. Data-driven tests

TestNG `@DataProvider` supplies multiple inputs to one scenario. Login validation uses
it for invalid credentials; responsive testing uses it for desktop and mobile metrics.
Give each row only the data needed to describe one independent case.

## 8. API tests

`ApiClient` owns the base URI, headers, and HTTP timeouts. `PostsApiTest` owns status,
content-type, payload, and business assertions. `post.schema.json` is a Draft 2020-12
contract validated for the single post and every collection item.

JSONPlaceholder fakes create operations. A successful POST response does not mean a new
record will be returned by a later GET.

Run API tests without a browser:

```powershell
.\mvnw.cmd test -Dgroups=api
```

## 9. Database tests

`SQLiteCustomerRepository.inMemory()` creates a new connection and schema. Foreign keys
are explicitly enabled for every connection. `@BeforeMethod` gives each test a fresh
repository, and `@AfterMethod(alwaysRun = true)` closes it.

The ClassicModels repository is different: it connects to a public external service,
marks the connection read-only, applies timeouts, and performs only SELECT queries. Its
tests are isolated in the `external` group because network availability is not
deterministic.

Use try-with-resources for every JDBC statement, result set, stream, and temporary
repository:

```java
try (PreparedStatement statement = connection.prepareStatement(sql)) {
    statement.setInt(1, customerNumber);
    try (ResultSet results = statement.executeQuery()) {
        // map the result
    }
}
```

## 10. Accessibility and responsive testing

The accessibility test runs axe rules tagged WCAG 2.0/2.1 A and AA. If a violation is
found, the failure includes its rule, impact, help, node count, and selectors. The full
result is attached to Allure. Do not add broad exclusions merely to make a build green.

Responsive tests use exact Chromium device metrics at 1440×900 and 390×844. JavaScript
is used only to read DOM measurements unavailable from ordinary WebDriver APIs.

Automated accessibility tests cannot judge every requirement. Perform keyboard,
screen-reader, zoom, reflow, focus-order, and human usability checks as well.

## 11. Groups, suites, and parallel execution

Groups select a purpose:

```powershell
.\mvnw.cmd test -Dgroups=smoke
.\mvnw.cmd test -Dgroups=regression
.\mvnw.cmd test -Dgroups=db
```

XML suites live in `src/test/resources/suites`. They define class scope, group filters,
browser parameters, and bounded parallelism:

```powershell
.\mvnw.cmd test "-DsuiteXmlFile=src/test/resources/suites/cross-browser.xml"
```

The cross-browser suite contains only browser tests. API and database tests must never
be repeated once per browser.

## 12. Debug a failure

Investigate in this order:

1. Read the TestNG assertion and stack trace.
2. Check `target/surefire-reports`.
3. Open the screenshot under `artifacts/screenshots`.
4. Inspect the Allure URL and browser-detail attachments.
5. Read `artifacts/logs/automation.log`.
6. Classify the failure as product behavior, test code, test data, browser/driver, or an
   unavailable public service.

Generate the interactive report with:

```bash
allure serve target/allure-results
```

## 13. Continuous integration

GitHub Actions installs Java 21, caches Maven dependencies, and runs the Maven Wrapper.
Chrome and Edge UI jobs run headlessly. API/SQLite and external ClassicModels checks use
separate jobs. Diagnostics are uploaded even when a test fails.

Keep local commands and workflow commands identical. A test that needs special manual
steps locally will usually be unreliable in CI.

## 14. Principles to keep

- Tests are independent and order-free.
- UI selectors stay in page objects.
- Setup and cleanup stay in lifecycle classes.
- API and database tests never create browsers.
- JDBC resources always close.
- External failures are reported, not silently skipped.
- Credentials and local paths are not hard-coded.
- Assertions are not weakened to make a test pass.
- The same Maven Wrapper commands run locally and in CI.
