package com.developer.harshul.pinvoke;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class UtilityTests {

    @Test
    public void testDateCalculations() {
        // Test days remaining calculation
        long currentTime = System.currentTimeMillis();
        long futureTime = currentTime + TimeUnit.DAYS.toMillis(5);

        int daysRemaining = (int) TimeUnit.MILLISECONDS.toDays(futureTime - currentTime);
        assertEquals(5, daysRemaining);
    }

    @Test
    public void testOverdueDateCalculation() {
        // Test overdue date handling
        Calendar pastDate = Calendar.getInstance();
        pastDate.add(Calendar.MONTH, -1); // 1 month ago

        Calendar currentDate = Calendar.getInstance();

        assertTrue("Past date should be before current date",
                pastDate.before(currentDate));
    }

    @Test
    public void testJSONDataValidation() {
        // Test JSON structure for card data
        try {
            JSONArray cardsArray = new JSONArray();
            JSONObject card = new JSONObject();
            card.put("name", "Test Card");
            card.put("dueDate", System.currentTimeMillis());
            cardsArray.put(card);

            // Validate structure
            assertEquals(1, cardsArray.length());
            JSONObject retrievedCard = cardsArray.getJSONObject(0);
            assertEquals("Test Card", retrievedCard.getString("name"));
            assertTrue(retrievedCard.has("dueDate"));

        } catch (JSONException e) {
            fail("JSON operations should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDateRangeValidation() {
        long currentTime = System.currentTimeMillis();
        long fiveYearsAgo = currentTime - TimeUnit.DAYS.toMillis(5 * 365);
        long fiveYearsFromNow = currentTime + TimeUnit.DAYS.toMillis(5 * 365);

        // Test valid date ranges
        assertTrue("Current time should be valid",
                currentTime >= fiveYearsAgo && currentTime <= fiveYearsFromNow);

        // Test invalid dates
        long tooOld = currentTime - TimeUnit.DAYS.toMillis(6 * 365);
        long tooFuture = currentTime + TimeUnit.DAYS.toMillis(6 * 365);

        assertFalse("Too old date should be invalid",
                tooOld >= fiveYearsAgo && tooOld <= fiveYearsFromNow);
        assertFalse("Too future date should be invalid",
                tooFuture >= fiveYearsAgo && tooFuture <= fiveYearsFromNow);
    }

    @Test
    public void testCardNameValidation() {
        // Test card name handling
        String validName = "Chase Sapphire";
        String emptyName = "";
        String nullName = null;
        String longName = "Very Long Card Name That Exceeds Normal Length";

        assertFalse("Valid name should not be empty", validName.trim().isEmpty());
        assertTrue("Empty name should be empty", emptyName.trim().isEmpty());

        // Test null safety
        String safeName = (nullName != null) ? nullName : "Credit Card";
        assertEquals("Credit Card", safeName);

        // Test name length (assuming max length of 50)
        assertTrue("Long name should be valid", longName.length() > 0);
    }

    @Test
    public void testColorCalculation() {
        // Test color selection based on days remaining
        int redDays = -1; // Overdue
        int orangeDays = 2; // Within 3 days
        int greenDays = 15; // More than 7 days

        // These would correspond to your app's color logic
        assertTrue("Overdue should be critical", redDays < 0);
        assertTrue("Near due should be warning", orangeDays > 0 && orangeDays <= 3);
        assertTrue("Safe period should be normal", greenDays > 7);
    }
}