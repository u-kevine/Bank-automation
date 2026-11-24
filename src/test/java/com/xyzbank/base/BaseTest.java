package com.xyzbank.base;

import com.xyzbank.data.Customer;
import com.xyzbank.data.DataGenerator;
import com.xyzbank.data.TestData;
import com.xyzbank.driver.DriverFactory;
import com.xyzbank.listeners.AllureSetupListener;
import com.xyzbank.listeners.KnownDefectListener;
import com.xyzbank.listeners.ProgressListener;
import com.xyzbank.pages.AccountPage;
import com.xyzbank.pages.HomePage;
import io.qameta.allure.Allure;
import io.qameta.allure.Issue;
import io.qameta.allure.Step;
import java.io.ByteArrayInputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners({AllureSetupListener.class, KnownDefectListener.class, ProgressListener.class})
public abstract class BaseTest {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<HomePage> HOME = new ThreadLocal<>();
    private static final Queue<WebDriver> ALL_DRIVERS = new ConcurrentLinkedQueue<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> ALL_DRIVERS.forEach(BaseTest::quietQuit)));
    }

    protected WebDriver driver() {
        return DRIVER.get();
    }

    protected HomePage home() {
        return HOME.get();
    }

    @BeforeMethod(alwaysRun = true)
    public void openApp() {
        WebDriver driver = DRIVER.get();
        if (driver == null || !resetState(driver)) {
            if (driver != null) {
                discard(driver);
            }
            driver = DriverFactory.create();
            DRIVER.set(driver);
            ALL_DRIVERS.add(driver);
        }
        HOME.set(openHome(driver));
    }

    @AfterMethod(alwaysRun = true)
    public void afterTest(ITestResult result) {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            return;
        }
        if (!result.isSuccess() || isKnownDefect(result)) {
            attachScreenshot(driver);
        }
        HOME.remove();
    }

    private boolean resetState(WebDriver driver) {
        try {
            try {
                driver.switchTo().alert().dismiss();
            } catch (NoAlertPresentException noAlert) {
                driver.getTitle();
            }
            driver.manage().deleteAllCookies();
            ((JavascriptExecutor) driver).executeScript(
                    "try { window.localStorage.clear(); window.sessionStorage.clear(); } catch (e) {}");
            driver.get("about:blank");
            return true;
        } catch (WebDriverException browserGone) {
            return false;
        }
    }

    private void discard(WebDriver driver) {
        ALL_DRIVERS.remove(driver);
        quietQuit(driver);
        DRIVER.remove();
    }

    private static void quietQuit(WebDriver driver) {
        try {
            driver.quit();
        } catch (WebDriverException alreadyClosed) {
            return;
        }
    }

    private HomePage openHome(WebDriver driver) {
        try {
            return new HomePage(driver).open();
        } catch (TimeoutException firstAttempt) {
            return new HomePage(driver).open();
        }
    }

    @Step("Precondition: create a new customer")
    protected Customer newCustomer() {
        Customer customer = DataGenerator.uniqueCustomer();
        home().loginAsManager().openAddCustomer()
                .addCustomer(customer.firstName(), customer.lastName(), customer.postCode());
        home().goHome();
        return customer;
    }

    @Step("Precondition: create a customer with a Dollar account")
    protected Customer customerWithAccount() {
        Customer customer = newCustomer();
        home().loginAsManager().openOpenAccount().openAccount(customer.fullName(), "Dollar");
        home().goHome();
        return customer;
    }

    @Step("Precondition: log in as a new customer with an account")
    protected AccountPage loginAsNewAccountHolder() {
        Customer customer = customerWithAccount();
        return home().goToCustomerLogin().login(customer.fullName());
    }

    @Step("Precondition: log in as an existing demo customer")
    protected AccountPage loginAsExistingCustomer() {
        var c = TestData.get("existing_customers").get(1);
        String name = c.get("first_name").asText() + " " + c.get("last_name").asText();
        return home().goToCustomerLogin().login(name);
    }

    private boolean isKnownDefect(ITestResult result) {
        return result.getMethod().getConstructorOrMethod().getMethod().isAnnotationPresent(Issue.class);
    }

    private void attachScreenshot(WebDriver driver) {
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("screenshot", "image/png", new ByteArrayInputStream(png), "png");
            Allure.addAttachment("url", driver.getCurrentUrl());
        } catch (WebDriverException e) {
            return;
        }
    }
}