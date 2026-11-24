package com.xyzbank.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ManagerPage extends BasePage {
    protected static final By ADD_CUSTOMER_TAB = By.xpath("//button[@ng-click='addCust()']");
    protected static final By OPEN_ACCOUNT_TAB = By.xpath("//button[@ng-click='openAccount()']");
    protected static final By CUSTOMERS_TAB = By.xpath("//button[@ng-click='showCust()']");

    public ManagerPage(WebDriver driver) {
        super(driver);
    }

    public ManagerPage waitLoaded() {
        find(ADD_CUSTOMER_TAB);
        return this;
    }

    public boolean tabsVisible() {
        return isVisible(ADD_CUSTOMER_TAB) && isVisible(OPEN_ACCOUNT_TAB) && isVisible(CUSTOMERS_TAB);
    }

    @Step("Open 'Add Customer' tab")
    public AddCustomerPage openAddCustomer() {
        click(ADD_CUSTOMER_TAB);
        return new AddCustomerPage(driver).waitLoaded();
    }

    @Step("Open 'Open Account' tab")
    public OpenAccountPage openOpenAccount() {
        click(OPEN_ACCOUNT_TAB);
        return new OpenAccountPage(driver).waitLoaded();
    }

    @Step("Open 'Customers' tab")
    public CustomersPage openCustomers() {
        click(CUSTOMERS_TAB);
        return new CustomersPage(driver).waitLoaded();
    }
}
