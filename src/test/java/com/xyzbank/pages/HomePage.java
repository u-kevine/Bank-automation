package com.xyzbank.pages;

import com.xyzbank.config.Config;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {
    private static final By CUSTOMER_LOGIN = By.xpath("//button[@ng-click='customer()']");
    private static final By MANAGER_LOGIN = By.xpath("//button[@ng-click='manager()']");
    private static final By HOME_BUTTON = By.cssSelector("button.home");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    @Step("Open XYZ Bank home page")
    public HomePage open() {
        driver.get(Config.BASE_URL);
        find(MANAGER_LOGIN);
        return this;
    }

    @Step("Go to home page")
    public HomePage goHome() {
        click(HOME_BUTTON);
        find(MANAGER_LOGIN);
        return this;
    }

    public boolean isAtStart() {
        return isVisible(MANAGER_LOGIN);
    }

    @Step("Log in as Bank Manager")
    public ManagerPage loginAsManager() {
        click(MANAGER_LOGIN);
        return new ManagerPage(driver).waitLoaded();
    }

    @Step("Go to Customer Login")
    public CustomerLoginPage goToCustomerLogin() {
        click(CUSTOMER_LOGIN);
        return new CustomerLoginPage(driver).waitLoaded();
    }
}
