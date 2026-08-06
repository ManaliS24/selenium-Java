# Selenium Java Demo QA

A production-style Java 21 automation portfolio covering browser UI, REST API,
database, responsive, and automated accessibility testing in one TestNG/Maven project.

## Systems under test

| Layer | Target | Coverage |
|---|---|---|
| UI | [SauceDemo](https://www.saucedemo.com/) | Login, negative authentication, cart, checkout, validation |
| API | [JSONPlaceholder](https://jsonplaceholder.typicode.com/) | Read, collection, filter, create, 404, JSON contract |
| Local database | In-memory SQLite | Persistence, unique/FK constraints, cascade, isolation |
| External database | CTU ClassicModels MariaDB | Read-only customer and CSV-baseline validation |
| Accessibility | SauceDemo login | axe-core WCAG 2.0/2.1 A and AA |
| Responsive | SauceDemo inventory | 1440×900 desktop and 390×844 mobile |

JSONPlaceholder simulates write operations. `POST /posts` returns an echoed response and
generated ID, but it does not persist the resource.

## Technology

- Java 21 LTS and Maven Wrapper
- Selenium WebDriver 4 and Selenium Manager
- TestNG and Maven Surefire
- REST Assured and Draft 2020-12 JSON Schema validation
- JDBC with SQLite and MariaDB drivers
- axe-core Selenium integration
- Allure TestNG
- SLF4J and Logback
- Jackson JSON and CSV processing

## Architecture

```text
src/main/java/com/portfolio/automation/
|-- api/       reusable HTTP client
|-- config/    layered configuration
|-- db/        JDBC repositories
|-- driver/    browser parsing, creation, and ThreadLocal ownership
`-- pages/     locators and browser interactions

src/test/java/com/portfolio/automation/
|-- accessibility/
|-- api/
|-- base/      browser lifecycle
|-- db/
|-- driver/
|-- listeners/ failure diagnostics
|-- responsive/
`-- ui/        business-level assertions

src/test/resources/
|-- data/
|-- schemas/
|-- suites/
`-- logback-test.xml
```

Each UI test gets a fresh browser and `ThreadLocal<WebDriver>` makes parallel TestNG
execution safe. API and database classes do not extend the UI base class, so they never
create a browser. Page objects own selectors and interactions; tests own assertions.

## Prerequisites

- Java 21 (`java -version`)
- Chrome and/or Microsoft Edge for UI execution
- Git
- Allure CLI only when generating an interactive local report

Maven does not need to be installed. The checked-in wrapper downloads the pinned Maven
distribution. Selenium Manager, included with Selenium, resolves compatible browser
drivers automatically; do not manually commit driver executables.

## Setup

### Windows PowerShell

```powershell
git clone https://github.com/ManaliS24/selenium-Java.git
cd selenium-Java
java -version
.\mvnw.cmd -DskipTests test
```

### macOS or Linux

```bash
git clone https://github.com/ManaliS24/selenium-Java.git
cd selenium-Java
chmod +x mvnw
java -version
./mvnw -DskipTests test
```

If a managed Windows machine points Maven at an unwritable cache, use a writable cache:

```powershell
$env:MAVEN_USER_HOME="$PWD\.m2"
.\mvnw.cmd "-Dmaven.repo.local=$PWD\.m2\repository" test
```

## Configuration

Values are resolved in this order:

1. Java system property, such as `-Dbrowser=edge`
2. Environment variable, such as `BROWSER=edge`
3. `src/main/resources/config.properties`
4. A safe framework default

| Environment variable | System property | Default |
|---|---|---|
| `UI_BASE_URL` | `uiBaseUrl` | `https://www.saucedemo.com` |
| `API_BASE_URL` | `apiBaseUrl` | `https://jsonplaceholder.typicode.com` |
| `E2E_USERNAME` | `username` | `standard_user` |
| `E2E_PASSWORD` | `password` | public demo credential |
| `DB_HOST` | `dbHost` | `relational.fel.cvut.cz` |
| `DB_PORT` | `dbPort` | `3306` |
| `DB_NAME` | `dbName` | `classicmodels` |
| `DB_USER` | `dbUser` | `guest` |
| `DB_PASSWORD` | `dbPassword` | public demo credential |
| `SELENIUM_TIMEOUT` | `timeout` | `10` seconds |
| `HEADLESS` | `headless` | `true` |
| `BROWSER` | `browser` | `chrome` |
| `WINDOW_WIDTH` | `windowWidth` | `1440` |
| `WINDOW_HEIGHT` | `windowHeight` | `900` |

`.env.example` documents the inputs. The framework does not automatically load `.env`;
export values in the shell or configure them in CI. Never commit private credentials.

## Commands

Use `mvnw.cmd` on Windows and `./mvnw` on macOS/Linux.

```powershell
# Compile test sources without executing tests
.\mvnw.cmd -DskipTests test

# Default discovery and execution
.\mvnw.cmd test

# Groups
.\mvnw.cmd test -Dgroups=smoke -Dbrowser=chrome
.\mvnw.cmd test -Dgroups=ui -Dbrowser=chrome
.\mvnw.cmd test -Dgroups=ui -Dbrowser=edge
.\mvnw.cmd test -Dgroups=ui -Dbrowser=chrome -Dheadless=false
.\mvnw.cmd test -Dgroups=api
.\mvnw.cmd test -Dgroups=db
.\mvnw.cmd test -Dgroups=external
.\mvnw.cmd test -Dgroups=accessibility -Dbrowser=chrome
.\mvnw.cmd test -Dgroups=responsive -Dbrowser=edge
.\mvnw.cmd test -Dgroups=regression -Dbrowser=chrome

# XML suites
.\mvnw.cmd test "-DsuiteXmlFile=src/test/resources/suites/smoke.xml"
.\mvnw.cmd test "-DsuiteXmlFile=src/test/resources/suites/regression.xml"
.\mvnw.cmd test "-DsuiteXmlFile=src/test/resources/suites/cross-browser.xml"
```

Available groups are `ui`, `api`, `db`, `accessibility`, `responsive`, `smoke`,
`regression`, and `external`. Suite files additionally cover UI, API, local database,
external database, accessibility, responsive, smoke, regression, and cross-browser runs.

The external group depends on a public service and network route. Availability or rate
limits can fail that group independently of the deterministic SQLite tests.

## Parallel and cross-browser execution

Regression and layer suites declare bounded TestNG thread counts. Every UI thread owns
its driver, and screenshots include the browser, timestamp, and thread ID. The
cross-browser suite runs Chrome and Edge test blocks concurrently but does not include
API or database classes, preventing browser-based duplication of non-UI tests.

Set `-Dheadless=false` for visible local debugging. CI always uses headless mode.

## Results and diagnostics

Surefire writes results to `target/surefire-reports`. Allure data is written to
`target/allure-results`:

```bash
allure serve target/allure-results
```

On UI failure the listener saves PNG evidence under `artifacts/screenshots` and attaches
the screenshot, current URL, and browser details to Allure. Logs are written to
`artifacts/logs/automation.log`; passwords are not logged.

The axe scan attaches its complete JSON result to Allure. Automated checks do not replace
keyboard-only testing, screen-reader testing, zoom/reflow review, or manual usability
assessment.

## Continuous integration

GitHub Actions runs on pushes, pull requests, and manual dispatch. Separate jobs:

- Exercise all UI groups in headless Chrome and Edge.
- Run API and isolated SQLite tests without browser setup.
- Run the external ClassicModels validation independently.
- Upload Allure results, Surefire reports, logs, and screenshots even after failures.

The workflow uses Java 21, Maven dependency caching, and the Maven Wrapper. Private
overrides belong in GitHub variables or secrets.

## Troubleshooting

- **Browser missing:** install stable Chrome or Edge, then rerun the matching browser.
- **Driver resolution fails:** verify internet/proxy access to Selenium Manager sources
  and that the browser executable is discoverable.
- **Browser/driver mismatch:** update the installed browser or Selenium dependency; do
  not add a hard-coded local driver path.
- **Edge CDP warning:** standard WebDriver tests can still run. CDP-dependent features
  require a Selenium release supporting that browser's major version.
- **Public service timeout:** rerun the affected API or `external` group and distinguish
  endpoint availability from assertion failures.
- **Allure command missing:** install the Allure CLI or inspect Surefire XML directly.
