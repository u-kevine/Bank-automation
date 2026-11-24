package com.xyzbank.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.xyzbank.base.BaseTest;
import com.xyzbank.pages.AccountPage;
import com.xyzbank.pages.TransactionsPage;
import com.xyzbank.pages.TransactionsPage.Transaction;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.util.List;
import org.testng.annotations.Test;

@Epic("XYZ Bank")
@Feature("US2 - Customer")
@Story("Transactions")
public class TransactionsTest extends BaseTest {
    private static boolean listed(List<Transaction> rows, int amount, String type) {
        return rows.stream().anyMatch(r -> r.amount() == amount && r.type().equals(type));
    }

    @Test(groups = {"customer", "regression"}, description = "A deposit appears in the transaction history")
    @Severity(SeverityLevel.CRITICAL)
    public void depositListed() {
        AccountPage account = loginAsExistingCustomer();
        account.deposit(250);
        account.waitForNewSecond();

        List<Transaction> rows = account.openTransactions().waitForTransactions(1);

        assertTrue(listed(rows, 250, "Credit"), "Deposit not listed in transactions: " + rows);
    }

    @Test(groups = {"customer", "regression"}, description = "A withdrawal appears in the transaction history")
    @Severity(SeverityLevel.CRITICAL)
    public void withdrawalListed() {
        AccountPage account = loginAsExistingCustomer();
        account.deposit(250);
        account.waitForNewSecond();
        account.withdraw(50);
        account.waitForNewSecond();

        List<Transaction> rows = account.openTransactions().waitForTransactions(2);

        assertTrue(listed(rows, 250, "Credit"), "Deposit missing: " + rows);
        assertTrue(listed(rows, 50, "Debit"), "Withdrawal missing: " + rows);
    }

    @Test(groups = {"customer", "regression"},
            description = "Customer cannot reset or alter transaction history")
    @Issue("DEF-03")
    @Severity(SeverityLevel.BLOCKER)
    public void historyCannotBeReset() {
        AccountPage account = loginAsExistingCustomer();
        int before = account.balance();
        account.deposit(250);
        account.waitForNewSecond();
        TransactionsPage page = account.openTransactions();
        List<Transaction> history = page.waitForTransactions(1);
        if (history.isEmpty()) {
            throw new IllegalStateException("Precondition failed: deposit not listed before Reset");
        }

        if (page.resetVisible()) {
            page.reset();
        }

        assertEquals(page.transactions(), history, "Transaction history changed after Reset");
        assertEquals(page.back().balance(), before + 250, "Balance changed after Reset");
    }

    @Test(groups = {"customer", "regression"}, description = "Back returns to the account page")
    @Severity(SeverityLevel.MINOR)
    public void backToAccount() {
        AccountPage account = loginAsExistingCustomer();

        assertFalse(account.openTransactions().back().welcomeName().isEmpty(),
                "Back did not return to the account page");
    }

    @Test(groups = {"customer", "regression"},
            description = "Transaction history is shown when opened right after a transaction")
    @Issue("DEF-04")
    @Severity(SeverityLevel.NORMAL)
    public void historyShownRightAfterTransaction() {
        throw new AssertionError("Known defect DEF-04 (see DEFECTS.md): the history is empty when it is opened in the "
                + "same second as the last transaction. This depends on split-second timing, so a reliable automated "
                + "check is not possible; it was confirmed with instrumented runs (evidence in DEFECTS.md).");
    }
}