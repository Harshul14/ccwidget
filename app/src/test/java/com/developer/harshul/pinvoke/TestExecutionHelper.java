package com.developer.harshul.pinvoke;

import com.developer.harshul.pinvoke.WidgetTestSuite;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestExecutionHelper {

    public static void runAllTests() {
        System.out.println("Running Credit Card Widget Test Suite...");

        Result result = JUnitCore.runClasses(WidgetTestSuite.class);

        System.out.println("Tests run: " + result.getRunCount());
        System.out.println("Failures: " + result.getFailureCount());
        System.out.println("Success rate: " +
                ((double)(result.getRunCount() - result.getFailureCount()) / result.getRunCount() * 100) + "%");

        if (result.getFailureCount() > 0) {
            System.out.println("\nFailures:");
            for (Failure failure : result.getFailures()) {
                System.out.println("- " + failure.getTestHeader());
                System.out.println("  " + failure.getMessage());
            }
        } else {
            System.out.println("\nAll tests passed! ✅");
        }
    }

    public static void runSpecificTestClass(Class<?> testClass) {
        Result result = JUnitCore.runClasses(testClass);

        System.out.println("Running " + testClass.getSimpleName());
        System.out.println("Tests: " + result.getRunCount() +
                ", Failures: " + result.getFailureCount());

        for (Failure failure : result.getFailures()) {
            System.out.println("FAIL: " + failure.getTestHeader() +
                    " - " + failure.getMessage());
        }
    }
}