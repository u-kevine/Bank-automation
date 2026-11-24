package com.xyzbank.pages;

import io.qameta.allure.Step;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CustomersPage extends ManagerPage {
    private static final By SEARCH = By.xpath("//input[@ng-model='searchCustomer']");
    private static final By ROWS = By.cssSelector("table tbody tr");
    private static final By DELETE_BUTTON = By.cssSelector("button[ng-click='deleteCust(cust)']");
    private static final By CELLS = By.tagName("td");

    public record CustomerRow(String firstName, String lastName, String postCode, List<String> accounts) {
    }

    public CustomersPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public CustomersPage waitLoaded() {
        find(SEARCH);
        return this;
    }

    @Step("Search customers for '{text}'")
    public CustomersPage search(String text) {
        type(SEARCH, text);
        return this;
    }

    public List<CustomerRow> rows() {
        List<CustomerRow> result = new ArrayList<>();
        for (WebElement row : findAll(ROWS)) {
            List<String> cells = cellTexts(row);
            if (cells.size() >= 4) {
                List<String> accounts = cells.get(3).isEmpty()
                        ? List.of() : Arrays.asList(cells.get(3).split("\\s+"));
                result.add(new CustomerRow(cells.get(0), cells.get(1), cells.get(2), accounts));
            }
        }
        return result;
    }

    public Optional<CustomerRow> findCustomer(String firstName, String lastName) {
        search(firstName);
        return rows().stream()
                .filter(r -> r.firstName().equals(firstName) && r.lastName().equals(lastName))
                .findFirst();
    }

    @Step("Delete customer {firstName} {lastName}")
    public boolean deleteCustomer(String firstName, String lastName) {
        search(firstName);
        for (WebElement row : findAll(ROWS)) {
            List<String> cells = cellTexts(row);
            if (cells.size() >= 2 && cells.get(0).equals(firstName) && cells.get(1).equals(lastName)) {
                row.findElement(DELETE_BUTTON).click();
                return true;
            }
        }
        return false;
    }

    private List<String> cellTexts(WebElement row) {
        return row.findElements(CELLS).stream().map(c -> c.getText().trim()).toList();
    }
}
