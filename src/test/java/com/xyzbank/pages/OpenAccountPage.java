package com.xyzbank.pages;

import io.qameta.allure.Step;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OpenAccountPage extends ManagerPage {
    private static final By CUSTOMER_SELECT = By.id("userSelect");
    private static final By CURRENCY_SELECT = By.id("currency");
    private static final By PROCESS = By.cssSelector("form button[type='submit']");

    public OpenAccountPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public OpenAccountPage waitLoaded() {
        find(CURRENCY_SELECT);
        return this;
    }

    public List<String> customerOptions() {
        return optionTexts(CUSTOMER_SELECT);
    }

    @Step("Open {currency} account for {customerName}")
    public String openAccount(String customerName, String currency) {
        selectByText(CUSTOMER_SELECT, customerName);
        selectByText(CURRENCY_SELECT, currency);
        click(PROCESS);
        return acceptAlertIfPresent();
    }
}
