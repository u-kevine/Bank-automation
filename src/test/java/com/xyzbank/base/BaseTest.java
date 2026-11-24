package com.xyzbank.base;

import com.xyzbank.data.Customer;
import com.xyzbank.data.DataGenerator;
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

    protected WebDriver driver() {
        return DRIVER.get();
    }

    protected HomePage home() {
        return HOME.get();
    }

    @BeforeMethod(alwaysRun = true)
    public void openApp() {
        WebDriver driver = DriverFactory.create();
        DRIVER.set(driver);
        HOME.set(openHome(driver));
    }

    private HomePage openHome(WebDriver driver) {
        try {
            return new HomePage(driver).open();
        } catch (TimeoutException firstAttempt) {
            return new HomePage(driver).open();
        }
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser(ITestResult result) {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            return;
        }
        try {
            if (!result.isSuccess() || isKnownDefect(result)) {
                attachScreenshot(driver);
            }
        } finally {
            driver.quit();
            DRIVER.remove();
            HOME.remove();
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