package com.xyzbank.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AddCustomerPage extends ManagerPage {
    private static final By FIRST_NAME = By.xpath("//input[@ng-model='fName']");
    private static final By LAST_NAME = By.xpath("//input[@ng-model='lName']");
    private static final By POST_CODE = By.xpath("//input[@ng-model='postCd']");
    private static final By SUBMIT = By.cssSelector("form button[type='submit']");

    public AddCustomerPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public AddCustomerPage waitLoaded() {
        find(FIRST_NAME);
        return this;
    }

    @Step("Add customer {firstName} {lastName} ({postCode})")
    public String addCustomer(String firstName, String lastName, String postCode) {
        type(FIRST_NAME, firstName);
        type(LAST_NAME, lastName);
        type(POST_CODE, postCode);
        click(SUBMIT);
        return acceptAlertIfPresent();
    }

    public String firstNameValidationMessage() {
        return find(FIRST_NAME).getDomProperty("validationMessage");
    }
}
