package com.developer.harshul.pinvoke;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Calendar;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class IntegrationTest {

    @Mock
    private AppWidgetManager mockAppWidgetManager;

    private Context context;
    private static final int TEST_WIDGET_ID = 789;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
    }

    @Test
    public void testFullWorkflow_CreateAndUpdateWidget() {
        // This test simulates the full workflow:
        // 1. Save configuration data
        // 2. Update widget with that data
        // 3. Verify widget was updated

        // Given - Save test data
        SharedPreferences prefs = context.getSharedPreferences("CCWidgetPrefs" + TEST_WIDGET_ID, Context.MODE_PRIVATE);
        JSONArray testData = createIntegrationTestData();

        prefs.edit()
                .putString("cards_data", testData.toString())
                .commit();

        // When - Update widget
        CreditCardWidgetProvider.updateAppWidget(context, mockAppWidgetManager, TEST_WIDGET_ID);

        // Then - Verify widget was updated
        verify(mockAppWidgetManager).updateAppWidget(eq(TEST_WIDGET_ID), any());
    }

    @Test
    public void testConfigActivityToWidgetFlow() {
        // Test the flow from configuration to widget update

        // Given - Configuration activity saves data
        SharedPreferences prefs = context.getSharedPreferences("CCWidgetPrefs" + TEST_WIDGET_ID, Context.MODE_PRIVATE);
        JSONArray configData = createIntegrationTestData();

        prefs.edit()
                .putString("cards_data", configData.toString())
                .commit();

        // When - Widget provider reads the saved data
        String savedData = prefs.getString("cards_data", "");

        // Then - Data should match what was saved
        assertFalse("Saved data should not be empty", savedData.isEmpty());

        try {
            JSONArray retrievedArray = new JSONArray(savedData);
            assertEquals("Should have saved 2 cards", 2, retrievedArray.length());
        } catch (JSONException e) {
            fail("Should be able to parse saved JSON: " + e.getMessage());
        }
    }

    private JSONArray createIntegrationTestData() {
        JSONArray cardsArray = new JSONArray();
        try {
            Calendar date1 = Calendar.getInstance();
            date1.add(Calendar.DAY_OF_MONTH, 5);

            Calendar date2 = Calendar.getInstance();
            date2.add(Calendar.DAY_OF_MONTH, 15);

            JSONObject card1 = new JSONObject();
            card1.put("name", "Integration Test Card 1");
            card1.put("dueDate", date1.getTimeInMillis());
            cardsArray.put(card1);

            JSONObject card2 = new JSONObject();
            card2.put("name", "Integration Test Card 2");
            card2.put("dueDate", date2.getTimeInMillis());
            cardsArray.put(card2);

        } catch (JSONException e) {
            fail("Failed to create integration test data");
        }
        return cardsArray;
    }
}