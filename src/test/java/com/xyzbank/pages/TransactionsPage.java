package com.xyzbank.pages;

import io.qameta.allure.Step;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TransactionsPage extends BasePage {
    private static final By ROWS = By.cssSelector("table tbody tr");
    private static final By BACK = By.xpath("//button[@ng-click='back()']");
    private static final By RESET = By.xpath("//button[@ng-click='reset()']");

    public record Transaction(String dateTime, int amount, String type) {
    }

    public TransactionsPage(WebDriver driver) {
        super(driver);
    }

    public TransactionsPage waitLoaded() {
        find(BACK);
        return this;
    }

    public List<Transaction> transactions() {
        List<Transaction> result = new ArrayList<>();
        for (WebElement row : findAll(ROWS)) {
            List<String> cells = row.findElements(By.tagName("td")).stream()
                    .map(c -> c.getText().trim()).toList();
            if (cells.size() >= 3) {
                result.add(new Transaction(cells.get(0), (int) Double.parseDouble(cells.get(1)), cells.get(2)));
            }
        }
        return result;
    }

    @Step("Wait for at least {count} transaction(s)")
    public List<Transaction> waitForTransactions(int count) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> transactions().size() >= count);
        } catch (TimeoutException e) {
        }
        return transactions();
    }

    public boolean resetVisible() {
        return isVisible(RESET, 2);
    }

    @Step("Click Reset")
    public TransactionsPage reset() {
        click(RESET);
        return this;
    }

    @Step("Go back to account")
    public AccountPage back() {
        click(BACK);
        return new AccountPage(driver).waitLoaded();
    }
}
