package com.developer.harshul.ccwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class CreditCardWidgetProviderTest {

    @Mock
    private Context mockContext;
    @Mock
    private AppWidgetManager mockAppWidgetManager;
    @Mock
    private SharedPreferences mockSharedPreferences;

    private CreditCardWidgetProvider widgetProvider;
    private static final int TEST_WIDGET_ID = 123;
    private static final String PREFS_NAME = "CCWidgetPrefs";
    private static final String CARDS_DATA_KEY = "cards_data";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        widgetProvider = new CreditCardWidgetProvider();

        when(mockContext.getPackageName()).thenReturn("com.developer.harshul.ccwidget");
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences);
    }

    @Test
    public void testOnUpdate_WithValidWidgetIds() {
        int[] widgetIds = {1, 2, 3};

        widgetProvider.onUpdate(mockContext, mockAppWidgetManager, widgetIds);

        // Verify update was called for each widget ID
        verify(mockContext, atLeast(3)).getSharedPreferences(anyString(), anyInt());
    }

    @Test
    public void testOnUpdate_WithEmptyWidgetIds() {
        int[] widgetIds = {};

        widgetProvider.onUpdate(mockContext, mockAppWidgetManager, widgetIds);

        // Should not crash and not call any preferences
        verify(mockContext, never()).getSharedPreferences(anyString(), anyInt());
    }

    @Test
    public void testOnUpdate_WithNullContext() {
        int[] widgetIds = {1};

        widgetProvider.onUpdate(null, mockAppWidgetManager, widgetIds);

        // Should not crash
        verify(mockAppWidgetManager, never()).updateAppWidget(anyInt(), any(RemoteViews.class));
    }

    @Test
    public void testOnUpdate_WithNullAppWidgetManager() {
        int[] widgetIds = {1};

        widgetProvider.onUpdate(mockContext, null, widgetIds);

        // Should still get preferences but not update widget
        verify(mockContext, atLeastOnce()).getSharedPreferences(anyString(), anyInt());
    }

    @Test
    public void testUpdateAppWidget_WithValidData() throws Exception {
        // Setup valid card data
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Test Card");
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(5));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(CARDS_DATA_KEY, "")).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, TEST_WIDGET_ID);

        verify(mockAppWidgetManager).updateAppWidget(eq(TEST_WIDGET_ID), any(RemoteViews.class));
    }

    @Test
    public void testUpdateAppWidget_WithEmptyData() {
        when(mockSharedPreferences.getString(CARDS_DATA_KEY, "")).thenReturn("");

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, TEST_WIDGET_ID);

        verify(mockAppWidgetManager).updateAppWidget(eq(TEST_WIDGET_ID), any(RemoteViews.class));
    }

    @Test
    public void testUpdateAppWidget_WithInvalidJson() {
        when(mockSharedPreferences.getString(CARDS_DATA_KEY, "")).thenReturn("invalid json");

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, TEST_WIDGET_ID);

        verify(mockAppWidgetManager).updateAppWidget(eq(TEST_WIDGET_ID), any(RemoteViews.class));
    }

    @Test
    public void testUpdateAppWidget_WithOverdueCard() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Overdue Card");
        cardObj.put("dueDate", System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(CARDS_DATA_KEY, "")).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, TEST_WIDGET_ID);

        verify(mockAppWidgetManager).updateAppWidget(eq(TEST_WIDGET_ID), any(RemoteViews.class));
    }

    @Test
    public void testUpdateAppWidget_WithTodayDueCard() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Today Card");
        cardObj.put("dueDate", System.currentTimeMillis());
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(CARDS_DATA_KEY, "")).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, TEST_WIDGET_ID);

        verify(mockAppWidgetManager).updateAppWidget(eq(TEST_WIDGET_ID), any(RemoteViews.class));
    }

    @Test
    public void testUpdateAppWidget_WithMultipleCards() throws Exception {
        JSONArray cardsArray = new JSONArray();

        // Card 1 - due in 10 days
        JSONObject card1 = new JSONObject();
        card1.put("name", "Card 1");
        card1.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10));
        cardsArray.put(card1);

        // Card 2 - due in 3 days (should be selected as nearest)
        JSONObject card2 = new JSONObject();
        card2.put("name", "Card 2");
        card2.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3));
        cardsArray.put(card2);

        when(mockSharedPreferences.getString(CARDS_DATA_KEY, "")).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, TEST_WIDGET_ID);

        verify(mockAppWidgetManager).updateAppWidget(eq(TEST_WIDGET_ID), any(RemoteViews.class));
    }

    @Test
    public void testOnDeleted_WithValidWidgetIds() {
        int[] widgetIds = {1, 2, 3};
        SharedPreferences.Editor mockEditor = mock(SharedPreferences.Editor.class);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.clear()).thenReturn(mockEditor);

        widgetProvider.onDeleted(mockContext, widgetIds);

        verify(mockContext, times(3)).getSharedPreferences(anyString(), anyInt());
        verify(mockEditor, times(3)).clear();
        verify(mockEditor, times(3)).apply();
    }

    @Test
    public void testOnDeleted_WithEmptyWidgetIds() {
        int[] widgetIds = {};

        widgetProvider.onDeleted(mockContext, widgetIds);

        verify(mockContext, never()).getSharedPreferences(anyString(), anyInt());
    }

    @Test
    public void testOnDeleted_WithException() {
        int[] widgetIds = {1};
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenThrow(new RuntimeException("Test exception"));

        // Should not crash
        widgetProvider.onDeleted(mockContext, widgetIds);

        verify(mockContext).getSharedPreferences(anyString(), anyInt());
    }
}