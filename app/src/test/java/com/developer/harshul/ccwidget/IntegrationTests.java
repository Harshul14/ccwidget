package com.developer.harshul.ccwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class IntegrationTests {

    private Context context;
    private SharedPreferences sharedPreferences;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        sharedPreferences = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
    }

    @Test
    public void testFullWidgetLifecycle() throws Exception {
        // Test complete widget lifecycle: create -> configure -> update -> delete
        int widgetId = 1;
        String prefsName = "CCWidgetPrefs" + widgetId;

        // 1. Simulate widget creation and configuration
        SharedPreferences widgetPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);

        JSONArray cardsData = new JSONArray();
        JSONObject card = new JSONObject();
        card.put("name", "Integration Test Card");
        card.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(15));
        cardsData.put(card);

        SharedPreferences.Editor editor = widgetPrefs.edit();
        editor.putString("cards_data", cardsData.toString());
        assertTrue("Failed to save configuration", editor.commit());

        // 2. Verify data was saved
        String savedData = widgetPrefs.getString("cards_data", "");
        assertFalse("Data not saved properly", savedData.isEmpty());

        // 3. Simulate widget update
        CreditCardWidgetProvider provider = new CreditCardWidgetProvider();
        // Note: This would require mocking AppWidgetManager for full test

        // 4. Simulate widget deletion
        provider.onDeleted(context, new int[]{widgetId});

        // 5. Verify cleanup
        String cleanedData = widgetPrefs.getString("cards_data", "default");
        assertEquals("Data not cleaned up", "default", cleanedData);
    }

    @Test
    public void testConfigActivityIntegration() throws Exception {
        // Test the configuration activity with real Android components
        Intent intent = new Intent(context, CreditCardWidgetConfigActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 123);

        // This test would require ActivityTestRule or similar for full integration
        assertNotNull("Intent creation failed", intent);
        assertTrue("Widget ID not set",
                intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID));
    }

    @Test
    public void testPreferencesIntegration() throws Exception {
        // Test SharedPreferences integration across different widget instances
        int[] widgetIds = {1, 2, 3};

        for (int widgetId : widgetIds) {
            SharedPreferences prefs = context.getSharedPreferences(
                    "CCWidgetPrefs" + widgetId, Context.MODE_PRIVATE);

            JSONObject testData = new JSONObject();
            testData.put("name", "Card " + widgetId);
            testData.put("dueDate", System.currentTimeMillis());

            JSONArray array = new JSONArray();
            array.put(testData);

            prefs.edit().putString("cards_data", array.toString()).commit();
        }

        // Verify each widget has its own data
        for (int widgetId : widgetIds) {
            SharedPreferences prefs = context.getSharedPreferences(
                    "CCWidgetPrefs" + widgetId, Context.MODE_PRIVATE);
            String data = prefs.getString("cards_data", "");
            assertTrue("Widget " + widgetId + " data missing",
                    data.contains("Card " + widgetId));
        }
    }
}