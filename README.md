# XYZ Bank – Selenium WebDriver Test Automation (Java)

[![UI tests](https://github.com/u-kevine/Bank-automation/actions/workflows/ci.yml/badge.svg)](https://github.com/u-kevine/Bank-automation/actions/workflows/ci.yml)

Automated UI tests for the [XYZ Bank app](https://www.globalsqa.com/angularJs-protractor/BankingProject/),
covering User Story 1 (Bank Manager) and User Story 2 (Customer).

**Latest test report:** https://u-kevine.github.io/Bank-automation/

**Stack:** Java 17 · Maven · TestNG · Selenium 4 · Page Object Model · Allure · Docker · GitHub Actions

## Project structure

```
Bank-automation/
├── .github/workflows/ci.yml          # CI: build, run in Docker, Allure report, GitHub Pages
├── pom.xml                           # dependencies and build settings (Maven)
├── src/test/java/com/xyzbank/
│   ├── config/Config.java            # settings from environment variables
│   ├── driver/DriverFactory.java     # local or remote (Docker) browser
│   ├── pages/                        # Page Object Model: all locators and page actions
│   │   ├── BasePage.java             # shared waits, clicks, typing, dropdowns, alerts
│   │   ├── HomePage.java
│   │   ├── ManagerPage.java          # manager tabs
│   │   ├── AddCustomerPage.java
│   │   ├── OpenAccountPage.java
│   │   ├── CustomersPage.java        # search and delete
│   │   ├── CustomerLoginPage.java
│   │   ├── AccountPage.java          # balance, deposit, withdraw, logout
│   │   └── TransactionsPage.java
│   ├── data/
│   │   ├── TestData.java             # reads testdata.json
│   │   ├── DataGenerator.java        # unique, letters-only customer names
│   │   └── Customer.java
│   ├── base/BaseTest.java            # browser per test, preconditions, screenshots
│   ├── listeners/
│   │   ├── AllureSetupListener.java  # prepares allure-results before the run
│   │   └── KnownDefectListener.java  # reports @Issue tests as known defects
│   └── tests/                        # test classes grouped by user story and feature
├── src/test/resources/
│   ├── testdata.json                 # inputs and expected messages
│   ├── allure.properties             # results folder and defect link pattern
│   └── categories.json               # groups failures in the report
├── DEFECTS.md                        # defect log with reproduction steps and evidence
├── Dockerfile                        # test-runner image
└── docker-compose.yml                # test runner + Selenium standalone Chrome
```

## Coverage

| User story | Area | Test class |
|---|---|---|
| US1 Bank Manager | Login | `ManagerLoginTest` |
| US1 Bank Manager | Add customer: valid, invalid names, invalid postcodes, required fields, duplicates | `AddCustomerTest` |
| US1 Bank Manager | Open account (each currency), no access without account, search, delete, no access after delete | `ManagerAccountsTest` |
| US2 Customer | Login, logout | `CustomerLoginTest` |
| US2 Customer | Deposit: valid, zero, negative | `DepositTest` |
| US2 Customer | Withdraw: within, exact, above balance, zero, negative | `WithdrawTest` |
| US2 Customer | Transactions listed, history cannot be reset | `TransactionsTest` |

## Run locally

Requires JDK 17+, Maven 3.9+ and Google Chrome.

```bash
mvn test                              # full suite (headless by default)
mvn test -Dgroups=smoke               # smoke tests only
mvn test -Dtest=DepositTest           # one test class
HEADLESS=false mvn test               # watch the browser (needs a screen)
BROWSER=firefox mvn test              # run in Firefox
```

Selenium Manager downloads the matching browser driver automatically.

## Run in Docker

```bash
mkdir -p allure-results
docker compose up --build --abort-on-container-exit --exit-code-from tests
docker compose down
```

Run a subset: `docker compose run --rm tests mvn -B test -Dgroups=smoke`.
Watch the browser live at port 7900 (noVNC, password `secret`).

## Allure report

Requires the Allure CLI (`npm install -g allure-commandline`).

```bash
allure generate allure-results --clean -o allure-report
allure open allure-report
```

The report shows results by user story and severity, step-by-step logs, a screenshot and URL for every
failure and known defect, the run environment, and links from defect tests to `DEFECTS.md`.

## CI/CD

On every push and pull request to `main`, GitHub Actions builds the images, runs the suite in Docker,
generates the Allure report with trend history, posts a results summary on the run, and (for `main`)
publishes the report to GitHub Pages. It can also be run manually from the **Actions** tab.

## Known defects

Tests for acceptance criteria the app does not meet carry `@Issue("DEF-xx")`. `KnownDefectListener` checks them after each run:

- **The requirement's assertion fails** (defect still present): the test is reported as **Skipped** with the reason
  "Known defect DEF-xx", plus a screenshot and a link to [DEFECTS.md](DEFECTS.md). The run stays green.
- **The test passes** (defect fixed): it is reported as **Failed** with "DEF-xx appears to be fixed", so it gets retested and closed.
- **Any other error** (a broken locator, a timeout): it stays **Failed** or **Broken**, so test problems never hide behind a defect.

In the Allure report, these appear under **Categories → Known defects (logged)**.
DEF-04 cannot be automated reliably, so it appears as a documented Skipped entry that points to its evidence.

| Defect | Severity | Summary | Test |
|---|---|---|---|
| DEF-01 | High | Customer names accept digits and special characters | `AddCustomerTest.rejectNonAlphabeticNames` |
| DEF-02 | High | Postcodes accept non-numeric characters | `AddCustomerTest.rejectNonNumericPostCode` |
| DEF-03 | Critical | Customer can reset (erase) transaction history | `TransactionsTest.historyCannotBeReset` |
| DEF-04 | Medium | History shows nothing when opened within the same second as a transaction | `TransactionsTest.historyShownRightAfterTransaction` (documented skip; timing-dependent) |

## Design decisions

- **Fresh browser per test.** The app keeps data in memory, so every test starts from the same state and no test depends on another.
- **Explicit waits only.** `BasePage` waits for elements and conditions; the one timed wait, `waitForNewSecond()`, works around DEF-04 and is called explicitly where needed.
- **Unique generated data.** New customers get random letters-only names, so tests never collide and never break the name rule they test.
- **Page objects act, tests judge.** Page methods return results (alert text, balances, rows); only tests make assertions.
