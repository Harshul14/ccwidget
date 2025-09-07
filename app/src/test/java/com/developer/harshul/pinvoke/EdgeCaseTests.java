package com.developer.harshul.pinvoke;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Calendar;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class EdgeCaseTests extends BaseTest {

    @Mock
    private AppWidgetManager mockAppWidgetManager;

    @Mock
    private SharedPreferences mockPrefs;

    @Test
    public void testWidget_WithExtremelyLongCardName() throws JSONException {
        // Given - Card with very long name
        String veryLongName = "This is an extremely long credit card name that should be handled gracefully by the widget without causing any display issues or crashes in the user interface";
        JSONArray cards = new JSONArray();
        cards.put(TestUtils.createTestCard(veryLongName, 10));

        when(mockPrefs.getString(anyString(), anyString())).thenReturn(cards.toString());
        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
                .thenReturn(mockPrefs);

        // When - Should not crash
        CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, 1);

        // Then
        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithSpecialCharactersInName() throws JSONException {
        // Given - Card with special characters
        String specialName = "Card with émojis 💳 & símböls ñ ü";
        JSONArray cards = new JSONArray();
        cards.put(TestUtils.createTestCard(specialName, 5));

        when(mockPrefs.getString(anyString(), anyString())).thenReturn(cards.toString());
        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
                .thenReturn(mockPrefs);

        // When
        CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, 1);

        // Then - Should handle special characters
        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithVeryFutureDates() throws JSONException {
        // Given - Card due far in the future
        Calendar distantFuture = Calendar.getInstance();
        distantFuture.add(Calendar.YEAR, 10); // 10 years from now

        JSONArray cards = new JSONArray();
        cards.put(TestUtils.createTestCard("Future Card",
                (int)((distantFuture.getTimeInMillis() - System.currentTimeMillis()) / (24 * 60 * 60 * 1000))));

        when(mockPrefs.getString(anyString(), anyString())).thenReturn(cards.toString());
        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
                .thenReturn(mockPrefs);

        // When
        CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, 1);

        // Then - Should handle future dates
        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithInvalidDateValues() throws JSONException {
        // Given - Card with invalid date
        JSONArray cards = new JSONArray();
        cards.put(TestUtils.createTestCardWithCustomDate("Invalid Date Card", -1)); // Invalid timestamp

        when(mockPrefs.getString(anyString(), anyString())).thenReturn(cards.toString());
        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
                .thenReturn(mockPrefs);

        // When - Should handle gracefully
        CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, 1);

        // Then
        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithEmptyCardName() throws JSONException {
        // Given - Card with empty name
        JSONArray cards = new JSONArray();
        cards.put(TestUtils.createTestCard("", 10));

        when(mockPrefs.getString(anyString(), anyString())).thenReturn(cards.toString());
        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
                .thenReturn(mockPrefs);

        // When
        CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, 1);

        // Then - Should default to "Credit Card"
        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithMalformedJson() {
        // Given - Malformed JSON structures
        String[] malformedJsons = {
                "{name: 'missing quotes'}",
                "[{name: 'test', dueDate:}]", // Missing value
                "[{name: 'test'}]", // Missing dueDate
                "[]", // Empty array
                "null",
                "{}" // Empty object instead of array
        };

        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
                .thenReturn(mockPrefs);

        for (String malformedJson : malformedJsons) {
            when(mockPrefs.getString(anyString(), anyString())).thenReturn(malformedJson);

            // When - Should handle malformed JSON gracefully
            CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, 1);

            // Then - Should not crash
            verify(mockAppWidgetManager, atLeast(1)).updateAppWidget(eq(1), any());
        }
    }

    // Helper method for creating test cards with custom dates
    private JSONObject createTestCardWithCustomDate(String name, long timestamp) throws JSONException {
        JSONObject card = new JSONObject();
        card.put("name", name);
        card.put("dueDate", timestamp);
        return card;
    }
}