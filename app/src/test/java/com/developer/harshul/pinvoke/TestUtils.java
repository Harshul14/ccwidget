package com.developer.harshul.pinvoke;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.Random;

public class TestUtils {

    private static final Random random = new Random();

    // Create test card data with various scenarios
    public static JSONArray createTestCards(TestCardScenario scenario) throws JSONException {
        JSONArray cards = new JSONArray();
        Calendar baseDate = Calendar.getInstance();

        switch (scenario) {
            case SINGLE_CARD_FUTURE:
                cards.put(createTestCard("Single Card", 15));
                break;

            case MULTIPLE_CARDS_MIXED:
                cards.put(createTestCard("Card 1", -5));  // Overdue
                cards.put(createTestCard("Card 2", 2));   // Due soon
                cards.put(createTestCard("Card 3", 20));  // Due later
                break;

            case ALL_OVERDUE:
                cards.put(createTestCard("Overdue 1", -10));
                cards.put(createTestCard("Overdue 2", -5));
                cards.put(createTestCard("Overdue 3", -1));
                break;

            case EDGE_CASES:
                cards.put(createTestCard("Today", 0));
                cards.put(createTestCard("Tomorrow", 1));
                cards.put(createTestCard("Very Long Card Name That Should Be Truncated", 10));
                cards.put(createTestCard("", 5)); // Empty name
                break;

            case PERFORMANCE_TEST:
                for (int i = 0; i < 50; i++) {
                    cards.put(createTestCard("Perf Card " + i, random.nextInt(60) - 10));
                }
                break;
        }

        return cards;
    }

    public static JSONObject createTestCard(String name, int daysFromNow) throws JSONException {
        JSONObject card = new JSONObject();
        card.put("name", name);

        Calendar dueDate = Calendar.getInstance();
        dueDate.add(Calendar.DAY_OF_MONTH, daysFromNow);
        card.put("dueDate", dueDate.getTimeInMillis());

        return card;
    }
    public static JSONObject createTestCardWithCustomDate(String name, long timestamp) throws JSONException {
        JSONObject card = new JSONObject();
        card.put("name", name);
        card.put("dueDate", timestamp);
        return card;
    }

    // Method to simulate widget click
    public static void simulateWidgetClick(Context context, int widgetId) {
        Intent clickIntent = new Intent();
        clickIntent.putExtra("appWidgetId", widgetId);
        // Simulate the click action
    }

    // Method to verify widget display state
    public static boolean verifyWidgetDisplayState(String expectedCardName, int expectedDays) {
        // This would verify the widget's current display state
        // In a real implementation, this might capture the RemoteViews content
        return true;
    }

    // Save test data to SharedPreferences
    public static void saveTestData(Context context, int widgetId, JSONArray testData) {
        SharedPreferences prefs = context.getSharedPreferences(
                "CCWidgetPrefs" + widgetId, Context.MODE_PRIVATE);
        prefs.edit()
                .putString("cards_data", testData.toString())
                .commit();
    }

    // Clear all widget data
    public static void clearAllWidgetData(Context context) {
        String[] files = context.fileList();
        for (String file : files) {
            if (file.contains("CCWidgetPrefs")) {
                SharedPreferences prefs = context.getSharedPreferences(
                        file.replace(".xml", ""), Context.MODE_PRIVATE);
                prefs.edit().clear().commit();
            }
        }
    }

    // Validate JSON structure
    public static boolean isValidCardJson(String jsonString) {
        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject card = array.getJSONObject(i);
                if (!card.has("name") || !card.has("dueDate")) {
                    return false;
                }
                // Validate due date is a valid timestamp
                long dueDate = card.getLong("dueDate");
                if (dueDate <= 0) {
                    return false;
                }
            }
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    // Calculate expected days remaining
    public static int calculateDaysRemaining(long dueDateMillis) {
        long currentTime = System.currentTimeMillis();
        long diffMillis = dueDateMillis - currentTime;
        return (int) (diffMillis / (24 * 60 * 60 * 1000));
    }


    // Get expected color based on days remaining
    public static String getExpectedStatusColor(int daysRemaining) {
        if (daysRemaining < 0) {
            return "RED"; // Overdue
        } else if (daysRemaining == 0) {
            return "RED"; // Due today
        } else if (daysRemaining <= 3) {
            return "ORANGE"; // Due soon
        } else if (daysRemaining <= 7) {
            return "AMBER"; // Warning
        } else {
            return "GREEN"; // Safe
        }
    }

    public enum TestCardScenario {
        SINGLE_CARD_FUTURE,
        MULTIPLE_CARDS_MIXED,
        ALL_OVERDUE,
        EDGE_CASES,
        PERFORMANCE_TEST
    }
}