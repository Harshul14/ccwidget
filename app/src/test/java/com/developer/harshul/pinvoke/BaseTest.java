package com.developer.harshul.pinvoke;

import android.content.Context;
import android.content.SharedPreferences;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = TestApplication.class)
public abstract class BaseTest {
    protected Context context;
    protected AutoCloseable mockitoCloseable;

    @Before
    public void baseSetUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        clearAllPreferences();
    }

    @After
    public void baseTearDown() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
        clearAllPreferences();
    }

    protected void clearAllPreferences() {
        // Clear all SharedPreferences to ensure test isolation
        String[] prefFiles = context.fileList();
        for (String prefFile : prefFiles) {
            if (prefFile.contains("CCWidgetPrefs")) {
                SharedPreferences prefs = context.getSharedPreferences(
                        prefFile.replace(".xml", ""), Context.MODE_PRIVATE);
                prefs.edit().clear().commit();
            }
        }
    }
}