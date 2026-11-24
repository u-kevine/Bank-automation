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
@Story("Withdraw")
public class WithdrawTest extends BaseTest {
    private static final String SUCCESS = TestData.message("withdraw_success");
    private static final int OPENING = TestData.intValue("withdraw", "opening_deposit");
    private static final int VALID = TestData.intValue("withdraw", "valid_amount");

    @DataProvider(name = "invalidAmounts")
    public Object[][] invalidAmounts() {
        return TestData.rows(TestData.ints("withdraw", "invalid_amounts"));
    }

    @Test(groups = {"smoke", "customer", "regression"}, description = "Withdraw within the balance and the balance updates")
    @Severity(SeverityLevel.CRITICAL)
    public void withdrawWithinBalance() {
        AccountPage account = loginAsExistingCustomer();
        int before = account.balance();
        account.deposit(OPENING);

        account.withdraw(VALID);

        assertEquals(account.message(), SUCCESS);
        assertEquals(account.balance(), before + OPENING - VALID);
    }

    @Test(groups = {"customer", "regression"}, description = "Withdraw the exact balance")
    @Severity(SeverityLevel.NORMAL)
    public void withdrawExactBalance() {
        AccountPage account = loginAsExistingCustomer();
        account.deposit(300);
        int fullBalance = account.balance();

        account.withdraw(fullBalance);

        assertEquals(account.message(), SUCCESS);
        assertEquals(account.balance(), 0);
    }

    @Test(groups = {"customer", "regression"}, description = "Withdrawal above the balance is rejected")
    @Severity(SeverityLevel.CRITICAL)
    public void withdrawMoreThanBalance() {
        AccountPage account = loginAsExistingCustomer();
        account.deposit(100);
        int balance = account.balance();

        account.withdraw(balance + 1);

        assertEquals(account.message(), TestData.message("withdraw_insufficient"));
        assertEquals(account.balance(), balance, "Balance changed after a rejected withdrawal");
    }

    @Test(groups = {"customer", "regression"}, dataProvider = "invalidAmounts", description = "Zero or negative withdrawal is rejected")
    @Severity(SeverityLevel.CRITICAL)
    public void withdrawInvalidAmount(int amount) {
        AccountPage account = loginAsExistingCustomer();
        account.deposit(100);
        int balance = account.balance();

        account.withdraw(amount);

        assertNotEquals(account.message(), SUCCESS, "Withdrawal of " + amount + " reported success");
        assertEquals(account.balance(), balance, "Balance changed after withdrawing " + amount);
    }
}