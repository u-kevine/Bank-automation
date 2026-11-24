package com.xyzbank.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.xyzbank.base.BaseTest;
import com.xyzbank.data.TestData;
import com.xyzbank.pages.AccountPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("XYZ Bank")
@Feature("US2 - Customer")
@Story("Login")
public class CustomerLoginTest extends BaseTest {
    @Test(groups = {"smoke", "customer", "regression"}, description = "Existing customer can log in and see account details")
    @Severity(SeverityLevel.BLOCKER)
    public void customerLogin() {
        JsonNode c = TestData.get("existing_customers").get(0);
        String name = c.get("first_name").asText() + " " + c.get("last_name").asText();

        AccountPage account = home.goToCustomerLogin().login(name);

        assertEquals(account.welcomeName(), name);
        assertTrue(account.accountNumber().matches("\\d+"), "Account number is not shown");
        assertTrue(TestData.strings("currencies").contains(account.currency()), "Unexpected currency");
    }

    @Test(groups = {"customer", "regression"}, description = "Logout returns to customer selection")
    @Severity(SeverityLevel.NORMAL)
    public void logout() {
        AccountPage account = loginAsNewAccountHolder();

        assertFalse(account.logout().customerNames().isEmpty(), "Customer selection is not shown after logout");
    }
}
