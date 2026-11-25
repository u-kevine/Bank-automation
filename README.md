# XYZ Bank – Selenium WebDriver Test Automation

Automated UI tests for the [XYZ Bank demo app](https://www.globalsqa.com/angularJs-protractor/BankingProject/),
covering User Story 1 (Bank Manager) and User Story 2 (Customer).

**Stack:** Java 17 · Maven · TestNG 7.10 · Selenium 4.27 · Page Object Model · Jackson (JSON test data) · Allure 2 · Docker · GitHub Actions

**Defect log:** [DEFECTS.md](DEFECTS.md)

---

## Project structure

```
Bank-automation/
├── .github/workflows/ci.yml          # CI: build, run in Docker, Allure report, GitHub Pages, result gate
├── pom.xml                           # dependencies, Surefire + AspectJ (for Allure @Step)
├── Dockerfile                        # test-runner image (Maven + JDK 17)
├── docker-compose.yml                # test runner + Selenium standalone Chrome
├── DEFECTS.md                        # defect log with reproduction steps and evidence
└── src/test/
    ├── java/com/xyzbank/
    │   ├── config/Config.java            # settings read from environment variables
    │   ├── driver/DriverFactory.java     # local Chrome/Firefox or RemoteWebDriver (Selenium Grid)
    │   ├── base/BaseTest.java            # browser lifecycle, state reset, preconditions, screenshots
    │   ├── pages/                        # Page Object Model: locators and page actions only
    │   │   ├── BasePage.java             # explicit waits, click, type, dropdowns, alerts
    │   │   ├── HomePage.java
    │   │   ├── ManagerPage.java          # manager tabs (parent of the three manager pages)
    │   │   ├── AddCustomerPage.java
    │   │   ├── OpenAccountPage.java
    │   │   ├── CustomersPage.java        # search and delete
    │   │   ├── CustomerLoginPage.java
    │   │   ├── AccountPage.java          # balance, deposit, withdraw, logout
    │   │   └── TransactionsPage.java     # history, reset, back
    │   ├── data/
    │   │   ├── TestData.java             # reads testdata.json
    │   │   ├── DataGenerator.java        # unique, letters-only customer names
    │   │   └── Customer.java
    │   ├── listeners/
    │   │   ├── AllureSetupListener.java  # cleans allure-results, writes environment + categories
    │   │   ├── KnownDefectListener.java  # relabels @Issue tests as known defects
    │   │   └── ProgressListener.java     # numbered PASS / FAIL / KNOWN lines in the console
    │   └── tests/                        # one class per feature
    └── resources/
        ├── testdata.json                 # inputs and expected messages
        ├── allure.properties             # results folder and defect link pattern
        └── categories.json               # failure categories for the report
```

---

## Coverage

27 test methods; data providers expand them to **37 test cases**.

| User story | Area | Test class | Cases |
|---|---|---|---|
| US1 Bank Manager | Login, Home button | `ManagerLoginTest` | 2 |
| US1 Bank Manager | Add customer: valid, non-alphabetic names (×3), non-numeric postcodes (×3), required field, duplicate | `AddCustomerTest` | 9 |
| US1 Bank Manager | Open account (Dollar, Pound, Rupee), new customer selectable, no transactions without account, search, delete existing / new customer, deleted customer cannot log in | `ManagerAccountsTest` | 9 |
| US2 Customer | Login, logout | `CustomerLoginTest` | 2 |
| US2 Customer | Deposit: valid amounts (1, 500, 99999), zero, negative | `DepositTest` | 5 |
| US2 Customer | Withdraw: within balance, exact balance, above balance, zero, negative | `WithdrawTest` | 5 |
| US2 Customer | Deposit and withdrawal listed, history cannot be reset, back button, history shown right after a transaction | `TransactionsTest` | 5 |

### Interpretation notes

- **"Customers should not be able to access their accounts until an account has been created."** The app lets any
  added customer log in; without an account it shows *"Please open an account with us."* and hides Deposit/Withdraw.
  The test (`noAccessWithoutAccount`) treats "access" as the ability to see account details and transact.
- **"Bank managers should be able to delete customer accounts."** The app has no account-level delete; it deletes
  the whole customer. The tests cover customer deletion and check that the deleted customer disappears from Customer Login.

### Test groups

| Group | What it runs |
|---|---|
| `smoke` | Manager login, add valid customer, open account (3 currencies), customer login, valid deposits, valid withdrawal |
| `manager` | All US1 tests |
| `customer` | All US2 tests |
| `regression` | Everything |

---

## Run locally

Requires JDK 17+, Maven 3.9+ and Google Chrome (or Firefox). Selenium Manager downloads the matching driver.

```bash
mvn test                              # full suite, headless Chrome
mvn test -Dgroups=smoke               # one group
mvn test -Dtest=DepositTest           # one class
mvn test -Dthreads=2                  # run test classes in parallel (default 1)
HEADLESS=false mvn test               # watch the browser (needs a display)
BROWSER=firefox mvn test              # Firefox instead of Chrome
```

### Configuration (environment variables)

| Variable | Default | Purpose |
|---|---|---|
| `BASE_URL` | the GlobalSQA demo URL | App under test |
| `BROWSER` | `chrome` | `chrome` or `firefox` |
| `HEADLESS` | `true` | Run without a visible window |
| `SELENIUM_REMOTE_URL` | *(empty)* | If set, run on a Selenium Grid instead of a local browser |
| `TIMEOUT` | `15` | Explicit-wait timeout in seconds |

> **Note:** Surefire is configured with `testFailureIgnore=true`, so `mvn test` exits with code 0 even when tests
> fail. Read the console summary or the Allure report for results. In CI, the last workflow step decides pass/fail
> (see [CI/CD](#cicd)).

---

## Run in Docker

```bash
mkdir -p allure-results
docker compose up --build --abort-on-container-exit --exit-code-from tests
docker compose down
```

- `selenium` runs `selenium/standalone-chrome:4.27.0` (up to 3 sessions, 2 GB shared memory, health-checked).
- `tests` builds from the `Dockerfile` and waits until the Grid is healthy, then runs `mvn -B test` against it.
- Results are written to `./allure-results` on the host through a volume.

Run a subset: `docker compose run --rm tests mvn -B test -Dgroups=smoke`
Watch the browser live: open http://localhost:7900 (noVNC, password `secret`).

---

## Allure report

Requires the Allure CLI (`npm install -g allure-commandline`).

```bash
allure serve allure-results                         # build and open a temporary report
# or
allure generate allure-results --clean -o allure-report
allure open allure-report
```

At the start of each run, `AllureSetupListener` empties `allure-results` and writes `environment.properties`
(Java, browser, Grid URL, base URL) and `categories.json`.

What the report contains:

- **Behaviors:** tests grouped by Epic → Feature (user story) → Story (feature area), with severity.
- **Steps:** every page action annotated with `@Step`, including the alert text returned by the app.
- **Attachments:** a screenshot and the current URL for every failed test and for every `@Issue` test.
- **Links:** each known-defect test links to its entry in `DEFECTS.md`.
- **Environment and Executor:** run settings, plus the GitHub Actions run number and link when built in CI.
- **Trend:** history from previous CI runs (loaded from the `gh-pages` branch).

### Reading the pass rate

Of the 37 cases, 8 test acceptance criteria the app does not meet (DEF-01 ×3, DEF-02 ×3, DEF-03, DEF-04).
These are reported as **Failed** by design, so a healthy run shows **29 passed / 8 failed (78.4%)**.
Any other failure is a new problem.

### Categories

| Category | Matches |
|---|---|
| Known product defects | Failed, message contains `Known defect DEF-xx` |
| Defects that appear fixed (retest) | Failed, message contains `DEF-xx appears to be fixed` |
| Unexpected failures | Failed tests that are neither a known defect nor an "appears fixed" result |
| Test or environment problems | Any broken test (exceptions other than assertion failures, e.g. timeouts) |

---

## Known defects

Tests for acceptance criteria the app does not meet carry `@Issue("DEF-xx")`. After each such test,
`KnownDefectListener` adjusts the result:

| What happened | Reported as | Message |
|---|---|---|
| The requirement's assertion failed (defect still present) | **Failed** | `Known defect DEF-xx (see DEFECTS.md): …` |
| The test passed (defect may be fixed) | **Failed** | `DEF-xx appears to be fixed: … remove @Issue("DEF-xx")` |
| Any other exception (locator, timeout, precondition) | **Broken** (unchanged) | Original error |

| Defect | Severity | Summary | Test |
|---|---|---|---|
| DEF-01 | High | Customer names accept digits and special characters | `AddCustomerTest.rejectNonAlphabeticNames` (3 data rows) |
| DEF-02 | High | Postcodes accept non-numeric characters | `AddCustomerTest.rejectNonNumericPostCode` (3 data rows) |
| DEF-03 | Critical | Customer can reset (erase) transaction history | `TransactionsTest.historyCannotBeReset` |
| DEF-04 | Medium | History shows nothing when opened within the same second as a transaction | `TransactionsTest.historyShownRightAfterTransaction` |

DEF-04 depends on split-second timing and cannot be reproduced reliably by automation. Its test is a documented
placeholder that always reports the defect; the evidence comes from instrumented runs recorded in `DEFECTS.md`.

---

## CI/CD

`.github/workflows/ci.yml` runs on every push and pull request to `main`, and manually from the **Actions** tab.
A newer run on the same branch cancels an older one.

1. Validate that `testdata.json` is well-formed JSON.
2. Build the images and run the suite with Docker Compose. Container logs are printed if this fails.
3. Load report history from `gh-pages`, add executor details, and generate the Allure report (Allure 2.32.0).
4. Write a results table to the run summary.
5. Upload the report as a build artifact (kept 14 days).
6. On `main`, publish the report to GitHub Pages.
7. **Result gate:** the job fails if no results were produced, or if any test is failed or broken **and** has no
   `DEF-` issue link. Tests linked to a known defect do not fail the build.

Steps 3–7 run even when earlier steps fail, so a report is produced for every run.

---

## Design decisions

- **One browser per thread, clean state per test.** The browser is reused between tests for speed. Before each
  test, `BaseTest` dismisses any open alert, clears cookies, `localStorage` and `sessionStorage`, and reloads the
  app, so every test starts from the demo's default data. A new browser is started only if the old one has crashed.
  Drivers are held in a `ThreadLocal`, so parallel runs (`-Dthreads`) are safe.
- **Explicit waits only.** `BasePage` waits for visibility, clickability, text and alerts. The single timed wait,
  `AccountPage.waitForNewSecond()`, works around DEF-04 and is called explicitly before opening the history.
- **Unique generated data.** New customers get random letters-only suffixes, so tests never collide with each other
  and never break the name rule under test. Fixed inputs and expected messages live in `testdata.json`.
- **Preconditions as steps.** `BaseTest` provides reusable setup (`newCustomer`, `customerWithAccount`,
  `loginAsNewAccountHolder`, `loginAsExistingCustomer`) that appears as named steps in the report.
- **Page objects act, tests judge.** Page methods return values (alert text, balances, rows); only test classes
  contain assertions.
