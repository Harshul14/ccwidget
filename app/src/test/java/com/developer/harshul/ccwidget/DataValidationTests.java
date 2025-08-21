package com.developer.harshul.ccwidget;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class DataValidationTests {

    @Test
    public void testJsonParsing_WithValidData() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Valid Card");
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));
        cardsArray.put(cardObj);

        String jsonString = cardsArray.toString();

        // Test that we can parse it back
        JSONArray parsed = new JSONArray(jsonString);
        assertEquals(1, parsed.length());

        JSONObject parsedCard = parsed.getJSONObject(0);
        assertEquals("Valid Card", parsedCard.getString("name"));
        assertTrue(parsedCard.getLong("dueDate") > System.currentTimeMillis());
    }

    @Test
    public void testJsonParsing_WithMissingName() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        // Missing name field
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));
        cardsArray.put(cardObj);

        String jsonString = cardsArray.toString();
        JSONArray parsed = new JSONArray(jsonString);
        JSONObject parsedCard = parsed.getJSONObject(0);

        // Should handle missing name gracefully
        String name = parsedCard.optString("name", "Default Card");
        assertEquals("Default Card", name);
    }

    @Test
    public void testJsonParsing_WithMissingDueDate() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Test Card");
        // Missing dueDate field
        cardsArray.put(cardObj);

        String jsonString = cardsArray.toString();
        JSONArray parsed = new JSONArray(jsonString);
        JSONObject parsedCard = parsed.getJSONObject(0);

        // Should handle missing due date gracefully
        long dueDate = parsedCard.optLong("dueDate", System.currentTimeMillis());
        assertTrue(dueDate > 0);
    }

    @Test
    public void testJsonParsing_WithInvalidDueDateType() throws Exception {
        String invalidJson = "[{\"name\":\"Test\",\"dueDate\":\"not a number\"}]";

        JSONArray parsed = new JSONArray(invalidJson);
        JSONObject parsedCard = parsed.getJSONObject(0);

        // Should handle invalid date type
        long dueDate = parsedCard.optLong("dueDate", System.currentTimeMillis());
        assertTrue(dueDate > 0);
    }

    @Test
    public void testDateCalculations_WithVariousDueDates() {
        long currentTime = System.currentTimeMillis();

        // Test today
        long todayDiff = currentTime - currentTime;
        int todayDays = (int) TimeUnit.MILLISECONDS.toDays(todayDiff);
        assertEquals(0, todayDays);

        // Test future dates
        long futureDiff = (currentTime + TimeUnit.DAYS.toMillis(5)) - currentTime;
        int futureDays = (int) TimeUnit.MILLISECONDS.toDays(futureDiff);
        assertEquals(5, futureDays);

        // Test past dates
        long pastDiff = (currentTime - TimeUnit.DAYS.toMillis(3)) - currentTime;
        int pastDays = (int) TimeUnit.MILLISECONDS.toDays(pastDiff);
        assertEquals(-3, pastDays);
    }

    @Test
    public void testDateFormatting_WithVariousLocales() {
        long testDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(15);

        // Test basic date formatting (would need to set locale for full test)
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("MMM dd");
        String formattedDate = formatter.format(new java.util.Date(testDate));

        assertNotNull(formattedDate);
        assertTrue(formattedDate.length() > 0);
        assertTrue(formattedDate.contains(" "));
    }
}