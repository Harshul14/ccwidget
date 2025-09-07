package com.developer.harshul.pinvoke;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class AccessibilityTest extends BaseTest {

    @Test
    public void testContentDescriptions() {
        // Test MainActivity
        MainActivity mainActivity = Robolectric.buildActivity(MainActivity.class)
                .create()
                .start()
                .resume()
                .get();

        Button addWidgetButton = mainActivity.findViewById(R.id.add_widget_button);
        assertNotNull("Add widget button should exist", addWidgetButton);

        // Test ConfigActivity
        Intent intent = new Intent();
        intent.putExtra("appWidgetId", 1);

        CreditCardWidgetConfigActivity configActivity = Robolectric.buildActivity(
                        CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        FloatingActionButton fab = configActivity.findViewById(R.id.add_card_button);
        if (fab != null) {
            assertNotNull("FAB should have content description", fab.getContentDescription());
        }
    }

    @Test
    public void testFocusableElements() {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class)
                .create()
                .start()
                .resume()
                .get();

        Button addWidgetButton = activity.findViewById(R.id.add_widget_button);
        assertTrue("Add widget button should be focusable", addWidgetButton.isFocusable());
    }
}
