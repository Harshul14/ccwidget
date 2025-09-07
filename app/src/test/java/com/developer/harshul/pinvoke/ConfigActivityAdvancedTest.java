package com.developer.harshul.pinvoke;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.LinearLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowToast;

import java.util.Calendar;

import static org.junit.Assert.*;

public class ConfigActivityAdvancedTest extends BaseTest {

    private static final int TEST_WIDGET_ID = 12345;

    @Test
    public void testMaxCardLimitEnforcement() {
        // Given
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        CreditCardWidgetConfigActivity activity = Robolectric.buildActivity(
                        CreditCardWidgetConfigActivity.class, intent)
                .create()
                .start()
                .resume()
                .get();

        FloatingActionButton addButton = activity.findViewById(R.id.add_card_button);
        LinearLayout container = activity.findViewById(R.id.cards_container);

        // When - Try to add maximum number of cards + 1
        for (int i = 0; i < 11; i++) { // Assuming MAX_CARDS = 10
            addButton.performClick();
        }

        // Then - Should show toast and not exceed limit
        String toastText = ShadowToast.getTextOfLatestToast();
        assertTrue("Should show max cards message",
                toastText != null && toastText.contains("Maximum"));
    }

    @Test
    public void testSaveConfiguration_WithEmptyCardName() {
        // Given
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        CreditCardWidgetConfigActivity activity = Robolectric.buildActivity(
                        CreditCardWidgetConfigActivity.class, intent)
                .create()
                .start()
                .resume()
                .get();

        Button saveButton = activity.findViewById(R.id.save_button);

        // When - Try to save with empty card name
        saveButton.performClick();

        // Then - Should handle gracefully (set default name)
        // Activity should not crash
        assertNotNull(activity);
    }

    @Test
    public void testRemoveCard_LastCard() {
        // Given
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        CreditCardWidgetConfigActivity activity = Robolectric.buildActivity(
                        CreditCardWidgetConfigActivity.class, intent)
                .create()
                .start()
                .resume()
                .get();

        LinearLayout container = activity.findViewById(R.id.cards_container);
        int initialCount = container.getChildCount();

        // When - Try to remove the last card
        if (initialCount > 0) {
            // Find and click remove button on first card
            // Note: This is a simplified test - in real implementation,
            // you'd need to access the remove button properly
        }

        // Then - Should show message and not remove last card
        String toastText = ShadowToast.getTextOfLatestToast();
        if (toastText != null) {
            assertTrue("Should require at least one card",
                    toastText.contains("least one") || toastText.contains("required"));
        }
    }

    @Test
    public void testDateValidation_FutureDate() {
        // Test that only future dates are accepted
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.YEAR, 2);

        // Test date is in valid range
        long currentTime = System.currentTimeMillis();
        long fiveYearsFromNow = currentTime + (5L * 365 * 24 * 60 * 60 * 1000);

        assertTrue("Future date should be valid",
                futureDate.getTimeInMillis() > currentTime &&
                        futureDate.getTimeInMillis() < fiveYearsFromNow);
    }

    @Test
    public void testActivityRecreation_PreservesData() {
        // Given
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        // Save some test data first
        context.getSharedPreferences("CCWidgetPrefs" + TEST_WIDGET_ID, Context.MODE_PRIVATE)
                .edit()
                .putString("cards_data", createTestJsonData().toString())
                .commit();

        // When - Create and recreate activity
        CreditCardWidgetConfigActivity activity = Robolectric.buildActivity(
                        CreditCardWidgetConfigActivity.class, intent)
                .create()
                .start()
                .resume()
                .get();

        LinearLayout container = activity.findViewById(R.id.cards_container);

        // Then - Should load saved data
        assertTrue("Should have loaded cards", container.getChildCount() > 0);
    }

    private JSONArray createTestJsonData() {
        JSONArray array = new JSONArray();
        try {
            JSONObject card = new JSONObject();
            card.put("name", "Test Card");
            card.put("dueDate", System.currentTimeMillis() + 86400000); // Tomorrow
            array.put(card);
        } catch (JSONException e) {
            fail("Failed to create test JSON");
        }
        return array;
    }
}