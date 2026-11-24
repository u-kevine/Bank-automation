package com.xyzbank.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.xyzbank.base.BaseTest;
import com.xyzbank.data.Customer;
import com.xyzbank.data.TestData;
import com.xyzbank.pages.AccountPage;
import com.xyzbank.pages.CustomersPage;
import com.xyzbank.pages.CustomersPage.CustomerRow;
import com.xyzbank.pages.ManagerPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.util.List;
import java.util.Optional;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Epic("XYZ Bank")
@Feature("US1 - Bank Manager")
public class ManagerAccountsTest extends BaseTest {
    @DataProvider(name = "currencies")
    public Object[][] currencies() {
        return TestData.rows(TestData.strings("currencies"));
    }

    private JsonNode existingCustomer() {
        return TestData.get("existing_customers").get(1);
    }

    @Test(groups = {"smoke", "manager", "regression"}, dataProvider = "currencies",
            description = "Open an account for a new customer, who can then use it")
    @Story("Create account")
    @Severity(SeverityLevel.CRITICAL)
    public void openAccountForNewCustomer(String currency) {
        Customer customer = newCustomer();
        ManagerPage manager = home().loginAsManager();

        String alert = manager.openOpenAccount().openAccount(customer.fullName(), currency);

        assertTrue(alert != null && alert.contains(TestData.message("account_created")), "Unexpected alert: " + alert);
        String accountNo = alert.substring(alert.lastIndexOf(':') + 1).trim();
        Optional<CustomerRow> row = manager.openCustomers().findCustomer(customer.firstName(), customer.lastName());
        assertTrue(row.isPresent() && row.get().accounts().contains(accountNo),
                "Account " + accountNo + " not listed for the customer");

        AccountPage account = home().goHome().goToCustomerLogin().login(customer.fullName());
        assertEquals(account.accountNumber(), accountNo);
        assertEquals(account.balance(), 0);
        assertEquals(account.currency(), currency);
    }

    @Test(groups = {"manager", "regression"}, description = "A newly added customer can be chosen when opening an account")
    @Story("Create account")
    @Severity(SeverityLevel.NORMAL)
    public void newCustomerAvailableForAccount() {
        Customer customer = newCustomer();

        List<String> options = home().loginAsManager().openOpenAccount().customerOptions();

        assertTrue(options.contains(customer.fullName()), "New customer is not in the Open Account list");
    }

    @Test(groups = {"manager", "regression"}, description = "Customer cannot transact before an account is created")
    @Story("Create account")
    @Severity(SeverityLevel.CRITICAL)
    public void noAccessWithoutAccount() {
        Customer customer = newCustomer();

        AccountPage account = home().goToCustomerLogin().login(customer.fullName());

        assertTrue(account.hasNoAccountMessage(), "'No account' message is not shown");
        assertEquals(account.noAccountMessage(), TestData.message("no_account"));
        assertFalse(account.canTransact(), "Deposit/Withdraw available without an account");
    }

    @Test(groups = {"manager", "regression"}, description = "Search customers by name")
    @Story("Delete account")
    @Severity(SeverityLevel.MINOR)
    public void searchCustomer() {
        String first = existingCustomer().get("first_name").asText();
        String last = existingCustomer().get("last_name").asText();
        CustomersPage customers = home().loginAsManager().openCustomers().search(first);

        List<CustomerRow> rows = customers.rows();

        assertFalse(rows.isEmpty(), "Search returned no rows");
        assertTrue(rows.stream().anyMatch(r -> r.firstName().equals(first) && r.lastName().equals(last)),
                "Searched customer not in results");
        for (CustomerRow r : rows) {
            String values = String.join(" ", r.firstName(), r.lastName(), r.postCode(),
                    String.join(" ", r.accounts())).toLowerCase();
            assertTrue(values.contains(first.toLowerCase()), "Row does not match search: " + r);
        }
    }

    @Test(groups = {"manager", "regression"}, description = "Delete an existing customer")
    @Story("Delete account")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteExistingCustomer() {
        String first = existingCustomer().get("first_name").asText();
        String last = existingCustomer().get("last_name").asText();
        CustomersPage customers = home().loginAsManager().openCustomers();

        boolean deleted = customers.deleteCustomer(first, last);

        assertTrue(deleted, "Customer row not found");
        assertTrue(customers.findCustomer(first, last).isEmpty(), "Customer is still listed after deletion");
    }

    @Test(groups = {"manager", "regression"}, description = "Delete a new customer who has an account")
    @Story("Delete account")
    @Severity(SeverityLevel.NORMAL)
    public void deleteNewCustomerWithAccount() {
        Customer customer = newCustomer();
        ManagerPage manager = home().loginAsManager();
        manager.openOpenAccount().openAccount(customer.fullName(), "Dollar");
        CustomersPage customers = manager.openCustomers();

        customers.deleteCustomer(customer.firstName(), customer.lastName());

        assertTrue(customers.findCustomer(customer.firstName(), customer.lastName()).isEmpty(),
                "Customer is still listed after deletion");
    }

    @Test(groups = {"manager", "regression"}, description = "A deleted customer can no longer log in")
    @Story("Delete account")
    @Severity(SeverityLevel.CRITICAL)
    public void deletedCustomerCannotLogIn() {
        Customer customer = customerWithAccount();
        home().loginAsManager().openCustomers().deleteCustomer(customer.firstName(), customer.lastName());

        List<String> loginNames = home().goHome().goToCustomerLogin().customerNames();

        assertFalse(loginNames.contains(customer.fullName()),
                "Deleted customer " + customer.fullName() + " can still log in");
    }
}