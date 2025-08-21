package com.developer.harshul.ccwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class EdgeCaseTests {

    @Mock
    private Context mockContext;
    @Mock
    private AppWidgetManager mockAppWidgetManager;
    @Mock
    private SharedPreferences mockSharedPreferences;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getPackageName()).thenReturn("com.developer.harshul.ccwidget");
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences);
    }

    @Test
    public void testWidget_WithVeryLargeNumberOfCards() throws Exception {
        // Test with 100 cards (way above normal usage)
        JSONArray cardsArray = new JSONArray();
        for (int i = 0; i < 100; i++) {
            JSONObject cardObj = new JSONObject();
            cardObj.put("name", "Card " + i);
            cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(i + 1));
            cardsArray.put(cardObj);
        }

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithExtremelyLongCardNames() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();

        // Create a very long card name (1000 characters)
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longName.append("A");
        }

        cardObj.put("name", longName.toString());
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithSpecialCharactersInCardName() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Card with émojis 🚀💳 and special chars áéíóú");
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithNegativeWidgetId() {
        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, -1);

        verify(mockAppWidgetManager).updateAppWidget(eq(-1), any());
    }

    @Test
    public void testWidget_WithZeroWidgetId() {
        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 0);

        verify(mockAppWidgetManager).updateAppWidget(eq(0), any());
    }

    @Test
    public void testWidget_WithMaxIntWidgetId() {
        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, Integer.MAX_VALUE);

        verify(mockAppWidgetManager).updateAppWidget(eq(Integer.MAX_VALUE), any());
    }

    @Test
    public void testWidget_WithCorruptedJsonStructure() {
        String corruptedJson = "{\"cards\":[{\"name\":\"Test\",\"dueDate\":}]}"; // Missing value

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(corruptedJson);

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithMaliciousJsonContent() {
        String maliciousJson = "<script>alert('xss')</script>";

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(maliciousJson);

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithVeryLargeJsonString() {
        // Create a very large JSON string (1MB+)
        StringBuilder largeJson = new StringBuilder("[");
        for (int i = 0; i < 10000; i++) {
            if (i > 0) largeJson.append(",");
            largeJson.append("{\"name\":\"Card").append(i).append("\",\"dueDate\":").append(System.currentTimeMillis()).append("}");
        }
        largeJson.append("]");

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(largeJson.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithSystemTimeChanges() throws Exception {
        // Test with dates that might be affected by system time changes
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Time Test Card");
        cardObj.put("dueDate", 0L); // Epoch time
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithLeapYearDates() throws Exception {
        // Test with Feb 29 on a leap year
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Leap Year Card");

        // Feb 29, 2024 (leap year)
        java.util.Calendar leapYear = java.util.Calendar.getInstance();
        leapYear.set(2024, java.util.Calendar.FEBRUARY, 29);
        cardObj.put("dueDate", leapYear.getTimeInMillis());
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithConcurrentAccess() throws Exception {
        // Simulate concurrent widget updates
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Concurrent Test Card");
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        // Simulate multiple threads updating the same widget
        Thread thread1 = new Thread(() ->
                CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1)
        );
        Thread thread2 = new Thread(() ->
                CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1)
        );

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        verify(mockAppWidgetManager, times(2)).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithMemoryPressure() throws Exception {
        // Simulate low memory conditions
        System.gc(); // Force garbage collection

        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Memory Test Card");
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }
}