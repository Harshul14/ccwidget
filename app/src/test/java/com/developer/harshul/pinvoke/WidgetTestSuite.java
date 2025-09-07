package com.developer.harshul.pinvoke;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CreditCardWidgetProviderTest.class,
        WidgetProviderAdvancedTest.class,
        CreditCardWidgetConfigActivityTest.class,
        ConfigActivityAdvancedTest.class,
        MainActivityTest.class,
        UtilityTests.class,
        IntegrationTest.class,
        PerformanceTest.class
})
public class WidgetTestSuite {
    // This class remains empty, it's used only as a holder for the above annotations
}