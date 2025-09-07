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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class CreditCardWidgetProviderTest {

    @Mock
    private AppWidgetManager mockAppWidgetManager;

    @Mock
    private SharedPreferences mockPrefs;

    @Mock
    private SharedPreferences.Editor mockEditor;

    private Context context;
    private CreditCardWidgetProvider widgetProvider;
    private static final int TEST_WIDGET_ID = 123;
    private static final String PREFS_NAME = "CCWidgetPrefs";
    private static final String CARDS_DATA_KEY = "cards_data";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = RuntimeEnvironment.getApplication();
        widgetProvider = new CreditCardWidgetProvider();

        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        doNothing().when(mockEditor).apply();
        when(mockEditor.commit()).thenReturn(true);
    }

    @Test
    public void testUpdateAppWidget_WithValidData() {
        // Given
        JSONArray cardsArray = createTestCardsData();
        when(mockPrefs.getString(CARDS_DATA_KEY, "")).thenReturn(cardsArray.toString());

        // Mock context.getSharedPreferences
        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(PREFS_NAME + TEST_WIDGET_ID, Context.MODE_PRIVATE))
                .thenReturn(mockPrefs);

        // When
        CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, TEST_WIDGET_ID);

        // Then
        verify(mockAppWidgetManager).updateAppWidget(eq(TEST_WIDGET_ID), any(RemoteViews.class));
    }

    @Test
    public void testUpdateAppWidget_WithEmptyData() {
        // Given
        when(mockPrefs.getString(CARDS_DATA_KEY, "")).thenReturn("");

        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(PREFS_NAME + TEST_WIDGET_ID, Context.MODE_PRIVATE))
                .thenReturn(mockPrefs);

        // When
        CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, TEST_WIDGET_ID);

        // Then
        verify(mockAppWidgetManager).updateAppWidget(eq(TEST_WIDGET_ID), any(RemoteViews.class));
    }

    @Test
    public void testUpdateAppWidget_WithInvalidJson() {
        // Given
        when(mockPrefs.getString(CARDS_DATA_KEY, "")).thenReturn("invalid json");

        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(PREFS_NAME + TEST_WIDGET_ID, Context.MODE_PRIVATE))
                .thenReturn(mockPrefs);

        // When
        CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, TEST_WIDGET_ID);

        // Then
        verify(mockAppWidgetManager).updateAppWidget(eq(TEST_WIDGET_ID), any(RemoteViews.class));
    }

    @Test
    public void testOnUpdate_MultipleWidgets() {
        // Given
        int[] widgetIds = {1, 2, 3};
        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
                .thenReturn(mockPrefs);
        when(mockPrefs.getString(CARDS_DATA_KEY, "")).thenReturn("");

        // When
        widgetProvider.onUpdate(spyContext, mockAppWidgetManager, widgetIds);

        // Wait for handler to process
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        verify(mockAppWidgetManager, times(widgetIds.length))
                .updateAppWidget(anyInt(), any(RemoteViews.class));
    }

    @Test
    public void testOnDeleted_ClearsPreferences() {
        // Given
        int[] widgetIds = {1, 2};
        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
                .thenReturn(mockPrefs);

        // When
        widgetProvider.onDeleted(spyContext, widgetIds);

        // Then
        verify(mockEditor, times(widgetIds.length)).clear();
        verify(mockEditor, times(widgetIds.length)).apply();
    }

    private JSONArray createTestCardsData() {
        JSONArray cardsArray = new JSONArray();
        try {
            // Create a test card with due date 15 days from now
            Calendar futureDate = Calendar.getInstance();
            futureDate.add(Calendar.DAY_OF_MONTH, 15);

            JSONObject card = new JSONObject();
            card.put("name", "Test Card");
            card.put("dueDate", futureDate.getTimeInMillis());
            cardsArray.put(card);
        } catch (JSONException e) {
            fail("Failed to create test data");
        }
        return cardsArray;
    }
}