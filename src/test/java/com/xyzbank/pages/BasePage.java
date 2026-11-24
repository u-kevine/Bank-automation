package com.xyzbank.pages;

import com.xyzbank.config.Config;
import io.qameta.allure.Allure;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(Config.TIMEOUT));
    }

    protected WebElement find(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected List<WebElement> findAll(By locator) {
        return driver.findElements(locator);
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, Object text) {
        WebElement element = find(locator);
        element.clear();
        element.sendKeys(String.valueOf(text));
    }

    protected String textOf(By locator) {
        return find(locator).getText().trim();
    }

    protected void selectByText(By locator, String text) {
        new Select(find(locator)).selectByVisibleText(text);
    }

    protected List<String> optionTexts(By locator) {
        return new Select(find(locator)).getOptions().stream()
                .map(option -> option.getText().trim())
                .toList();
    }

    protected boolean isVisible(By locator, int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected boolean isVisible(By locator) {
        return isVisible(locator, 3);
    }

    protected void waitForText(By locator, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    protected String acceptAlertIfPresent() {
        try {
            Alert alert = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.alertIsPresent());
            String text = alert.getText();
            alert.accept();
            Allure.addAttachment("alert_text", text);
            return text;
        } catch (TimeoutException | NoAlertPresentException e) {
            return null;
        }
    }
}
