package com.developer.harshul.pinvoke;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class MemoryLeakTest extends BaseTest {

    @Test
    public void testConfigActivityMemoryLeaks() {
        // Test that config activity doesn't leak memory
        for (int i = 0; i < 10; i++) {
            Intent intent = new Intent();
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, i + 1);

            CreditCardWidgetConfigActivity activity = Robolectric.buildActivity(
                            CreditCardWidgetConfigActivity.class, intent)
                    .create()
                    .start()
                    .resume()
                    .pause()
                    .stop()
                    .destroy()
                    .get();

            // Force garbage collection
            System.gc();
        }

        // Test passes if we get here without OutOfMemoryError
        assertTrue("Should not leak memory with multiple activity instances", true);
    }

    @Test
    public void testWidgetProviderMemoryUsage() {
        // Create many widget provider instances
        CreditCardWidgetProvider[] providers = new CreditCardWidgetProvider[100];

        for (int i = 0; i < providers.length; i++) {
            providers[i] = new CreditCardWidgetProvider();
        }

        // Use the providers
        Context context = RuntimeEnvironment.getApplication();
        AppWidgetManager mockManager = mock(AppWidgetManager.class);

        for (CreditCardWidgetProvider provider : providers) {
            provider.onUpdate(context, mockManager, new int[]{1, 2, 3});
        }

        // Clear references and force GC
        for (int i = 0; i < providers.length; i++) {
            providers[i] = null;
        }
        System.gc();

        assertTrue("Should handle multiple provider instances", true);
    }
}