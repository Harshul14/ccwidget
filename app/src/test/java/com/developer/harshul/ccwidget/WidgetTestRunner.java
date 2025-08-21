package com.developer.harshul.ccwidget;

import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

public class WidgetTestRunner extends RobolectricTestRunner {
    public WidgetTestRunner(Class<?> testClass) throws Exception {
        super(testClass);
    }

    @Override
    protected Config buildGlobalConfig() {
        return Config.Builder.defaults()
                .setSdk(33)
                .setApplication(TestApplication.class)
                .build();
    }
}