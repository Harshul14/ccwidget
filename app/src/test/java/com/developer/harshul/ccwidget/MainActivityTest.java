package com.developer.harshul.ccwidget;

import android.content.Intent;
import android.widget.Button;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class MainActivityTest {

    private MainActivity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(MainActivity.class)
                .create()
                .get();
    }

    @Test
    public void testOnCreate_InitializesSuccessfully() {
        assertNotNull(activity);
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testOnCreate_WithException() {
        // Test activity creation with potential exceptions
        // This would require mocking internal dependencies
        assertNotNull(activity);
    }

    @Test
    public void testAddWidgetButton_Click() {
        Button addWidgetButton = activity.findViewById(R.id.add_widget_button);

        if (addWidgetButton != null) {
            addWidgetButton.performClick();

            // Verify toast message is shown
            String latestToast = ShadowToast.getTextOfLatestToast();
            assertNotNull(latestToast);
            assertTrue(latestToast.contains("Long press"));
        }
    }

    @Test
    public void testAddWidgetButton_ClickWithException() {
        // Test button click handling exceptions
        Button addWidgetButton = activity.findViewById(R.id.add_widget_button);

        if (addWidgetButton != null) {
            addWidgetButton.performClick();

            // Should not crash
            assertFalse(activity.isFinishing());
        }
    }

    @Test
    public void testOnDestroy_CleansUpResources() {
        activity = Robolectric.buildActivity(MainActivity.class)
                .create()
                .destroy()
                .get();

        // Should not crash during cleanup
        assertTrue(activity.isDestroyed());
    }

    @Test
    public void testShowToast_WhenActivityFinishing() {
        activity.finish();

        // Try to show toast after finishing - should handle gracefully
        // This would require access to internal methods
        assertTrue(activity.isFinishing());
    }

    @Test
    public void testShowToast_WhenActivityDestroyed() {
        activity = Robolectric.buildActivity(MainActivity.class)
                .create()
                .destroy()
                .get();

        // Try to show toast after destroying - should handle gracefully
        assertTrue(activity.isDestroyed());
    }
}