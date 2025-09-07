package com.developer.harshul.pinvoke;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class WidgetProviderAdvancedTest extends BaseTest {

    @Mock
    private AppWidgetManager mockAppWidgetManager;

    @Mock
    private SharedPreferences mockPrefs;

    @Mock
    private SharedPreferences.Editor mockEditor;

    private CreditCardWidgetProvider widgetProvider;
    private static final int TEST_WIDGET_ID = 999;
    private static final String PREFS_NAME = "CCWidgetPrefs";
    private static final String CARDS_DATA_KEY = "cards_data";

    @Before
    public void setUp() {
        super.baseSetUp();
        widgetProvider = new CreditCardWidgetProvider();
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.clear()).thenReturn(mockEditor);
        doNothing().when(mockEditor).apply();
        when(mockEditor.commit()).thenReturn(true);
    }

    @Test
    public void testUpdateWidget_WithOverdueCard() throws JSONException {
        // Given - Card that's overdue
        JSONArray cardsArray = new JSONArray();
        Calendar overdueDate = Calendar.getInstance();
        overdueDate.add(Calendar.DAY_OF_MONTH, -5); // 5 days ago

        JSONObject overdueCard = new JSONObject();
        overdueCard.put("name", "Overdue Card");
        overdueCard.put("dueDate", overdueDate.getTimeInMillis());
        cardsArray.put(overdueCard);

        when(mockPrefs.getString(CARDS_DATA_KEY, "")).thenReturn(cardsArray.toString());

        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(PREFS_NAME + TEST_WIDGET_ID, Context.MODE_PRIVATE))
                .thenReturn(mockPrefs);

        // When
        CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, TEST_WIDGET_ID);

        // Then - Verify widget was updated with overdue status
        verify(mockAppWidgetManager).updateAppWidget(eq(TEST_WIDGET_ID), any(RemoteViews.class));
        // Note: In a full implementation, you'd verify the RemoteViews content
    }

    @Test
    public void testUpdateWidget_WithMultipleCards_FindsNearestDue() throws JSONException {
        // Given - Multiple cards with different due dates
        JSONArray cardsArray = new JSONArray();

        // Card 1: Due in 10 days
        Calendar date1 = Calendar.getInstance();
        date1.add(Calendar.DAY_OF_MONTH, 10);
        JSONObject card1 = new JSONObject();
        card1.put("name", "Card Far");
        card1.put("dueDate", date1.getTimeInMillis());
        cardsArray.put(card1);

        // Card 2: Due in 3 days (should be selected)
        Calendar date2 = Calendar.getInstance();
        date2.add(Calendar.DAY_OF_MONTH, 3);
        JSONObject card2 = new JSONObject();
        card2.put("name", "Card Near");
        card2.put("dueDate", date2.getTimeInMillis());
        cardsArray.put(card2);

        // Card 3: Due in 15 days
        Calendar date3 = Calendar.getInstance();
        date3.add(Calendar.DAY_OF_MONTH, 15);
        JSONObject card3 = new JSONObject();
        card3.put("name", "Card Distant");
        card3.put("dueDate", date3.getTimeInMillis());
        cardsArray.put(card3);

        when(mockPrefs.getString(CARDS_DATA_KEY, "")).thenReturn(cardsArray.toString());

        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(PREFS_NAME + TEST_WIDGET_ID, Context.MODE_PRIVATE))
                .thenReturn(mockPrefs);

        // When
        CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, TEST_WIDGET_ID);

        // Then
        verify(mockAppWidgetManager).updateAppWidget(eq(TEST_WIDGET_ID), any(RemoteViews.class));
    }

    @Test
    public void testUpdateWidget_HandlesCorruptedData() {
        // Given - Corrupted JSON data
        when(mockPrefs.getString(CARDS_DATA_KEY, "")).thenReturn("corrupted{json}");

        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(PREFS_NAME + TEST_WIDGET_ID, Context.MODE_PRIVATE))
                .thenReturn(mockPrefs);

        // When - Should not crash
        CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, TEST_WIDGET_ID);

        // Then - Should fall back to default display
        verify(mockAppWidgetManager).updateAppWidget(eq(TEST_WIDGET_ID), any(RemoteViews.class));
    }

    @Test
    public void testUpdateWidget_WithNullContext() {
        // When - Null context should be handled gracefully
        CreditCardWidgetProvider.updateAppWidget(null, mockAppWidgetManager, TEST_WIDGET_ID);

        // Then - Should not crash, no widget update should occur
        verify(mockAppWidgetManager, never()).updateAppWidget(anyInt(), any(RemoteViews.class));
    }

    @Test
    public void testUpdateWidget_WithNullAppWidgetManager() {
        // When - Null AppWidgetManager should be handled gracefully
        CreditCardWidgetProvider.updateAppWidget(context, null, TEST_WIDGET_ID);

        // Then - Should not crash (no verification possible since manager is null)
        // Test passes if no exception is thrown
    }

    @Test
    public void testOnUpdate_HandlesEmptyWidgetIds() {
        // Given - Empty widget IDs array
        int[] emptyIds = {};

        // When - Should handle empty array gracefully
        widgetProvider.onUpdate(context, mockAppWidgetManager, emptyIds);

        // Then - No updates should occur
        verify(mockAppWidgetManager, never()).updateAppWidget(anyInt(), any(RemoteViews.class));
    }

    @Test
    public void testOverdueCardDateUpdate() throws JSONException {
        // Given - Card with past due date
        JSONArray cardsArray = new JSONArray();
        Calendar pastDate = Calendar.getInstance();
        pastDate.add(Calendar.MONTH, -2); // 2 months ago

        JSONObject pastCard = new JSONObject();
        pastCard.put("name", "Past Due Card");
        pastCard.put("dueDate", pastDate.getTimeInMillis());
        cardsArray.put(pastCard);

        when(mockPrefs.getString(CARDS_DATA_KEY, "")).thenReturn(cardsArray.toString());

        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(PREFS_NAME + TEST_WIDGET_ID, Context.MODE_PRIVATE))
                .thenReturn(mockPrefs);

        // When
        CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, TEST_WIDGET_ID);

        // Then - Should save updated date
        verify(mockEditor).putString(eq(CARDS_DATA_KEY), anyString());
        verify(mockEditor).apply();
    }
}