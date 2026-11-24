package com.xyzbank.listeners;

import io.qameta.allure.Issue;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ProgressListener implements ITestListener {

    private static final AtomicInteger DONE = new AtomicInteger();

    @Override
    public void onStart(ITestContext context) {
        System.out.println("Running " + context.getAllTestMethods().length + " test methods "
                + "(data-driven tests run once per data row)...");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        print("PASS        ", result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Issue issue = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Issue.class);
        print(issue != null ? "KNOWN " + issue.value() : "FAIL        ", result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        print("SKIP        ", result);
    }

    private static void print(String status, ITestResult result) {
        String name = result.getMethod().getDescription();
        if (name == null || name.isEmpty()) {
            name = result.getMethod().getMethodName();
        }
        Object[] params = result.getParameters();
        String data = params == null || params.length == 0 ? ""
                : " [" + Arrays.stream(params).map(String::valueOf).collect(Collectors.joining(", ")) + "]";
        long seconds = Math.max(0, (result.getEndMillis() - result.getStartMillis()) / 1000);
        System.out.printf("%3d. %s %s%s (%ds)%n", DONE.incrementAndGet(), status, name, data, seconds);
    }
}