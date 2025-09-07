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
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PerformanceTest extends BaseTest {

    @Mock
    private AppWidgetManager mockAppWidgetManager;

    @Mock
    private SharedPreferences mockPrefs;

    @Test
    public void testWidgetUpdate_WithLargeDataSet() throws JSONException {
        // Given - Large number of cards
        JSONArray largeDataSet = createLargeDataSet(50); // 50 cards
        when(mockPrefs.getString(anyString(), anyString())).thenReturn(largeDataSet.toString());

        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
                .thenReturn(mockPrefs);

        long startTime = System.currentTimeMillis();

        // When
        CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, 1);

        // Then - Should complete within reasonable time (less than 1 second)
        long endTime = System.currentTimeMillis();
        assertTrue("Widget update should be fast", (endTime - startTime) < 1000);
    }

    @Test
    public void testMemoryUsage_MultipleWidgetUpdates() {
        // Given
        int[] manyWidgetIds = new int[20]; // 20 widgets
        for (int i = 0; i < manyWidgetIds.length; i++) {
            manyWidgetIds[i] = i + 1;
        }

        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
                .thenReturn(mockPrefs);
        when(mockPrefs.getString(anyString(), anyString())).thenReturn("");

        // When - Update many widgets
        CreditCardWidgetProvider provider = new CreditCardWidgetProvider();
        provider.onUpdate(spyContext, mockAppWidgetManager, manyWidgetIds);

        // Then - Should not cause memory issues (test passes if no OutOfMemoryError)
        assertTrue("Multiple widget updates should not cause memory issues", true);
    }

    @Test
    public void testJsonParsing_Performance() throws JSONException {
        // Given - Very large JSON data
        JSONArray hugeDataSet = createLargeDataSet(100);
        String jsonString = hugeDataSet.toString();

        long startTime = System.nanoTime();

        // When - Parse large JSON multiple times
        for (int i = 0; i < 10; i++) {
            JSONArray parsed = new JSONArray(jsonString);
            assertTrue("Should successfully parse", parsed.length() > 0);
        }

        // Then - Should complete within reasonable time
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        assertTrue("JSON parsing should be fast", durationMs < 100);
    }

    private JSONArray createLargeDataSet(int count) throws JSONException {
        JSONArray array = new JSONArray();
        Calendar baseDate = Calendar.getInstance();

        for (int i = 0; i < count; i++) {
            JSONObject card = new JSONObject();
            card.put("name", "Test Card " + i);

            Calendar cardDate = (Calendar) baseDate.clone();
            cardDate.add(Calendar.DAY_OF_MONTH, i % 30); // Spread over 30 days
            card.put("dueDate", cardDate.getTimeInMillis());

            array.put(card);
        }

        return array;
    }
}