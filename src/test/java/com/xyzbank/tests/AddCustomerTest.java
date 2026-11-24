package com.xyzbank.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.xyzbank.base.BaseTest;
import com.xyzbank.data.Customer;
import com.xyzbank.data.DataGenerator;
import com.xyzbank.data.TestData;
import com.xyzbank.pages.AddCustomerPage;
import com.xyzbank.pages.CustomersPage.CustomerRow;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.util.Optional;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Epic("XYZ Bank")
@Feature("US1 - Bank Manager")
@Story("Add customer")
public class AddCustomerTest extends BaseTest {
    private static final String CUSTOMER_ADDED = TestData.message("customer_added");

    @DataProvider(name = "invalidNames")
    public Object[][] invalidNames() {
        JsonNode names = TestData.get("invalid_names");
        Object[][] rows = new Object[names.size()][];
        for (int i = 0; i < names.size(); i++) {
            JsonNode n = names.get(i);
            rows[i] = new Object[] {n.get("id").asText(), n.get("first_name").asText(), n.get("last_name").asText()};
        }
        return rows;
    }

    @DataProvider(name = "invalidPostCodes")
    public Object[][] invalidPostCodes() {
        return TestData.rows(TestData.strings("invalid_post_codes"));
    }

    @Test(groups = {"smoke", "manager", "regression"}, description = "Add a customer with valid data")
    @Severity(SeverityLevel.CRITICAL)
    public void addCustomerValid() {
        JsonNode base = TestData.get("valid_customer");
        Customer customer = DataGenerator.uniqueCustomer(base.get("first_name").asText(),
                base.get("last_name").asText(), base.get("post_code").asText());
        AddCustomerPage addPage = home().loginAsManager().openAddCustomer();

        String alert = addPage.addCustomer(customer.firstName(), customer.lastName(), customer.postCode());

        assertTrue(alert != null && alert.contains(CUSTOMER_ADDED), "Unexpected alert: " + alert);
        Optional<CustomerRow> row = addPage.openCustomers().findCustomer(customer.firstName(), customer.lastName());
        assertTrue(row.isPresent(), "New customer is missing from the Customers list");
        assertEquals(row.get().postCode(), customer.postCode());
    }

    @Test(groups = {"manager", "regression"}, dataProvider = "invalidNames",
            description = "Reject customer names that are not alphabetic")
    @Issue("DEF-01")
    @Severity(SeverityLevel.CRITICAL)
    public void rejectNonAlphabeticNames(String id, String firstName, String lastName) {
        AddCustomerPage addPage = home().loginAsManager().openAddCustomer();

        String alert = addPage.addCustomer(firstName, lastName, "10001");

        assertFalse(alert != null && alert.contains(CUSTOMER_ADDED),
                "[" + id + "] Name with invalid characters was accepted: " + alert);
    }

    @Test(groups = {"manager", "regression"}, dataProvider = "invalidPostCodes",
            description = "Reject postcodes that are not numeric")
    @Issue("DEF-02")
    @Severity(SeverityLevel.CRITICAL)
    public void rejectNonNumericPostCode(String postCode) {
        Customer customer = DataGenerator.uniqueCustomer(postCode);
        AddCustomerPage addPage = home().loginAsManager().openAddCustomer();

        String alert = addPage.addCustomer(customer.firstName(), customer.lastName(), postCode);

        assertFalse(alert != null && alert.contains(CUSTOMER_ADDED),
                "Postcode '" + postCode + "' was accepted: " + alert);
    }

    @Test(groups = {"manager", "regression"}, description = "Required fields are enforced")
    @Severity(SeverityLevel.NORMAL)
    public void requiredFirstName() {
        AddCustomerPage addPage = home().loginAsManager().openAddCustomer();

        String alert = addPage.addCustomer("", "Lovelace", "10001");

        assertNull(alert, "Form submitted with an empty first name: " + alert);
        String message = addPage.firstNameValidationMessage();
        assertNotNull(message, "No validation message on empty first name");
        assertFalse(message.isEmpty(), "No validation message on empty first name");
    }

    @Test(groups = {"manager", "regression"}, description = "Duplicate customer is rejected")
    @Severity(SeverityLevel.NORMAL)
    public void duplicateCustomerRejected() {
        Customer customer = DataGenerator.uniqueCustomer();
        AddCustomerPage addPage = home().loginAsManager().openAddCustomer();
        addPage.addCustomer(customer.firstName(), customer.lastName(), customer.postCode());

        String secondAlert = addPage.addCustomer(customer.firstName(), customer.lastName(), customer.postCode());

        assertTrue(secondAlert != null && secondAlert.contains(TestData.message("duplicate_customer")),
                "Duplicate customer was not rejected: " + secondAlert);
    }
}