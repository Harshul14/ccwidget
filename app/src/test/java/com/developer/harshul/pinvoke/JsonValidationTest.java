package com.developer.harshul.pinvoke;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Calendar;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class JsonValidationTest {

    @Test
    public void testValidJsonStructure() throws JSONException {
        // Given - Valid JSON structure
        JSONArray validArray = new JSONArray();
        JSONObject card = new JSONObject();
        card.put("name", "Test Card");
        card.put("dueDate", System.currentTimeMillis());
        validArray.put(card);

        // When
        String jsonString = validArray.toString();
        JSONArray parsedArray = new JSONArray(jsonString);

        // Then
        assertEquals(1, parsedArray.length());
        JSONObject parsedCard = parsedArray.getJSONObject(0);
        assertEquals("Test Card", parsedCard.getString("name"));
        assertTrue(parsedCard.has("dueDate"));
        assertTrue(parsedCard.getLong("dueDate") > 0);
    }

    @Test
    public void testJsonWithMissingFields() {
        // Given - JSON with missing required fields
        try {
            JSONObject incompleteCard = new JSONObject();
            incompleteCard.put("name", "Incomplete Card");
            // Missing dueDate field

            JSONArray array = new JSONArray();
            array.put(incompleteCard);

            // When - Parsing should work but validation should catch missing field
            String jsonString = array.toString();
            assertFalse("Should detect missing dueDate field",
                    TestUtils.isValidCardJson(jsonString));

        } catch (JSONException e) {
            fail("JSON creation should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testJsonWithExtraFields() throws JSONException {
        // Given - JSON with extra fields (should be acceptable)
        JSONObject cardWithExtras = new JSONObject();
        cardWithExtras.put("name", "Card with Extras");
        cardWithExtras.put("dueDate", System.currentTimeMillis());
        cardWithExtras.put("extraField", "should be ignored");
        cardWithExtras.put("anotherExtra", 12345);

        JSONArray array = new JSONArray();
        array.put(cardWithExtras);

        // When
        String jsonString = array.toString();

        // Then - Should be valid despite extra fields
        assertTrue("Should accept JSON with extra fields",
                TestUtils.isValidCardJson(jsonString));
    }

    @Test
    public void testJsonDateFormats() throws JSONException {
        // Test various date formats
        long[] testDates = {
                System.currentTimeMillis(),                    // Current time
                System.currentTimeMillis() + 86400000,        // Tomorrow
                System.currentTimeMillis() - 86400000,        // Yesterday
                1609459200000L,                               // Jan 1, 2021
                4102444800000L                                // Jan 1, 2100
        };

        for (long testDate : testDates) {
            JSONObject card = new JSONObject();
            card.put("name", "Date Test Card");
            card.put("dueDate", testDate);

            JSONArray array = new JSONArray();
            array.put(card);

            String jsonString = array.toString();
            assertTrue("Should handle date: " + testDate,
                    TestUtils.isValidCardJson(jsonString));
        }
    }

    @Test
    public void testJsonWithUnicodeCharacters() throws JSONException {
        // Given - Card names with various Unicode characters
        String[] unicodeNames = {
                "普通话信用卡",           // Chinese
                "Tarjeta Crédito",      // Spanish with accents
                "Кредитная карта",      // Cyrillic
                "クレジットカード",        // Japanese
                "💳 Emoji Card 🏦",     // Emojis
                "Card with ñ ü ç é"     // Various accents
        };

        for (String name : unicodeNames) {
            JSONObject card = new JSONObject();
            card.put("name", name);
            card.put("dueDate", System.currentTimeMillis());

            JSONArray array = new JSONArray();
            array.put(card);

            String jsonString = array.toString();

            // Round trip test
            JSONArray reparsed = new JSONArray(jsonString);
            assertEquals("Unicode name should survive round trip",
                    name, reparsed.getJSONObject(0).getString("name"));
        }
    }
}