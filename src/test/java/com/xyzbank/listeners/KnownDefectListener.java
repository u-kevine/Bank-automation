package com.xyzbank.listeners;

import io.qameta.allure.Issue;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;

public class KnownDefectListener implements IInvokedMethodListener {
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        if (!method.isTestMethod()) {
            return;
        }
        Issue issue = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Issue.class);
        if (issue == null) {
            return;
        }
        String id = issue.value();

        if (result.getStatus() == ITestResult.FAILURE && result.getThrowable() instanceof AssertionError) {
            result.setStatus(ITestResult.SKIP);
            result.setThrowable(new SkipException(
                    "Known defect " + id + " (see DEFECTS.md): " + result.getThrowable().getMessage()));
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            result.setStatus(ITestResult.FAILURE);
            result.setThrowable(new AssertionError(id + " appears to be fixed: the requirement now passes. "
                    + "Retest it, close the defect and remove @Issue(\"" + id + "\") from this test."));
        }
    }
}
