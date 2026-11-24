package com.xyzbank.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import com.xyzbank.base.BaseTest;
import com.xyzbank.data.TestData;
import com.xyzbank.pages.AccountPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Epic("XYZ Bank")
@Feature("US2 - Customer")
@Story("Deposit")
public class DepositTest extends BaseTest {
    private static final String SUCCESS = TestData.message("deposit_success");

    @DataProvider(name = "validAmounts")
    public Object[][] validAmounts() {
        return TestData.rows(TestData.ints("deposit", "valid_amounts"));
    }

    @DataProvider(name = "invalidAmounts")
    public Object[][] invalidAmounts() {
        return TestData.rows(TestData.ints("deposit", "invalid_amounts"));
    }

    @Test(groups = {"smoke", "customer", "regression"}, dataProvider = "validAmounts",
            description = "Deposit a valid amount and the balance updates")
    @Severity(SeverityLevel.CRITICAL)
    public void depositValidAmount(int amount) {
        AccountPage account = loginAsNewAccountHolder();
        int before = account.balance();

        account.deposit(amount);

        assertEquals(account.message(), SUCCESS);
        assertEquals(account.balance(), before + amount);
    }

    @Test(groups = {"customer", "regression"}, dataProvider = "invalidAmounts", description = "Zero or negative deposit is rejected")
    @Severity(SeverityLevel.CRITICAL)
    public void depositInvalidAmount(int amount) {
        AccountPage account = loginAsNewAccountHolder();
        int before = account.balance();

        account.deposit(amount);

        assertNotEquals(account.message(), SUCCESS, "Deposit of " + amount + " reported success");
        assertEquals(account.balance(), before, "Balance changed after depositing " + amount);
    }
}
