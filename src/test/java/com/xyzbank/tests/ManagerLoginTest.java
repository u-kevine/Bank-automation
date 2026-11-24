package com.xyzbank.tests;

import static org.testng.Assert.assertTrue;

import com.xyzbank.base.BaseTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("XYZ Bank")
@Feature("US1 - Bank Manager")
@Story("Login")
public class ManagerLoginTest extends BaseTest {
    @Test(groups = {"smoke", "manager", "regression"}, description = "Bank Manager can log in")
    @Severity(SeverityLevel.BLOCKER)
    public void managerLogin() {
        assertTrue(home.loginAsManager().tabsVisible(), "Manager tabs are not all visible");
    }

    @Test(groups = {"manager", "regression"}, description = "Home button returns to the start screen")
    @Severity(SeverityLevel.MINOR)
    public void homeButtonReturnsToStart() {
        home.loginAsManager();
        home.goHome();
        assertTrue(home.isAtStart(), "Start screen not shown");
    }
}
