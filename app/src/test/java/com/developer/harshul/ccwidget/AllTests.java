package com.developer.harshul.ccwidget;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        CreditCardWidgetProviderTest.class,
        CreditCardWidgetConfigActivityTest.class,
        MainActivityTest.class,
        EdgeCaseTests.class,
        PerformanceTests.class,
        SecurityTests.class,
        BoundaryValueTests.class,
        LocalizationTests.class,
        IntegrationTests.class,
        ErrorHandlingTests.class,
        DataValidationTests.class,
        AccessibilityTests.class
})
public class AllTests {
    // Test suite to run all tests together
}