package com.xyzbank.pages;

import io.qameta.allure.Step;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AccountPage extends BasePage {
    private static final By WELCOME_NAME = By.cssSelector("span.fontBig");
    private static final By NO_ACCOUNT_MSG = By.xpath("//span[@ng-show='noAccount']");
    private static final By ACCOUNT_INFO = By.xpath("//div[@ng-hide='noAccount']//strong");
    private static final By TRANSACTIONS_TAB = By.xpath("//button[@ng-click='transactions()']");
    private static final By DEPOSIT_TAB = By.xpath("//button[@ng-click='deposit()']");
    private static final By WITHDRAW_TAB = By.xpath("//button[@ng-click='withdrawl()']");
    private static final By FORM_LABEL = By.cssSelector("form label");
    private static final By AMOUNT = By.xpath("//input[@ng-model='amount']");
    private static final By SUBMIT = By.cssSelector("form button[type='submit']");
    private static final By MESSAGE = By.cssSelector("span.error");
    private static final By LOGOUT = By.cssSelector("button.logout");

    private long lastTransactionAtNanos = 0;

    public AccountPage(WebDriver driver) {
        super(driver);
    }

    public AccountPage waitLoaded() {
        find(WELCOME_NAME);
        return this;
    }

    public String welcomeName() {
        return textOf(WELCOME_NAME);
    }

    public boolean hasNoAccountMessage() {
        return isVisible(NO_ACCOUNT_MSG);
    }

    public String noAccountMessage() {
        return textOf(NO_ACCOUNT_MSG);
    }

    public boolean canTransact() {
        return isVisible(DEPOSIT_TAB, 2);
    }

    private List<String> info() {
        find(ACCOUNT_INFO);
        return findAll(ACCOUNT_INFO).stream().map(WebElement::getText).map(String::trim).toList();
    }

    public String accountNumber() {
        return info().get(0);
    }

    public int balance() {
        return (int) Double.parseDouble(info().get(1));
    }

    public String currency() {
        return info().get(2);
    }

    private void submitAmount(Object amount) {
        type(AMOUNT, amount);
        click(SUBMIT);
        lastTransactionAtNanos = System.nanoTime();
    }

    @Step("Deposit {amount}")
    public AccountPage deposit(int amount) {
        click(DEPOSIT_TAB);
        waitForText(FORM_LABEL, "Deposited");
        submitAmount(amount);
        return this;
    }

    @Step("Withdraw {amount}")
    public AccountPage withdraw(int amount) {
        click(WITHDRAW_TAB);

        waitForText(FORM_LABEL, "Withdrawn");
        submitAmount(amount);
        return this;
    }

    @Step("Wait until the next action falls in a new second")
    public AccountPage waitForNewSecond() {
        long minGapNanos = 1_100_000_000L;
        long elapsed = System.nanoTime() - lastTransactionAtNanos;
        if (elapsed < minGapNanos) {
            try {
                Thread.sleep((minGapNanos - elapsed) / 1_000_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return this;
    }

    public String message() {
        return isVisible(MESSAGE) ? textOf(MESSAGE) : "";
    }

    @Step("Open transactions")
    public TransactionsPage openTransactions() {
        click(TRANSACTIONS_TAB);
        return new TransactionsPage(driver).waitLoaded();
    }

    @Step("Log out")
    public CustomerLoginPage logout() {
        click(LOGOUT);
        return new CustomerLoginPage(driver).waitLoaded();
    }
}
