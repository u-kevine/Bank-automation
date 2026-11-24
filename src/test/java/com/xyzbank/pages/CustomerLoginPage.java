package com.xyzbank.pages;

import io.qameta.allure.Step;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CustomerLoginPage extends BasePage {
    private static final String PLACEHOLDER = "---Your Name---";
    private static final By USER_SELECT = By.id("userSelect");
    private static final By LOGIN = By.cssSelector("form button[type='submit']");

    public CustomerLoginPage(WebDriver driver) {
        super(driver);
    }

    public CustomerLoginPage waitLoaded() {
        find(USER_SELECT);
        return this;
    }

    public List<String> customerNames() {
        return optionTexts(USER_SELECT).stream()
                .filter(name -> !name.isEmpty() && !name.equals(PLACEHOLDER))
                .toList();
    }

    @Step("Log in as customer {fullName}")
    public AccountPage login(String fullName) {
        selectByText(USER_SELECT, fullName);
        click(LOGIN);
        return new AccountPage(driver).waitLoaded();
    }
}
