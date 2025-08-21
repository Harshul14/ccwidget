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
public class BoundaryValueTests {

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
    public void testWidget_WithMinLongValue() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Min Value Card");
        cardObj.put("dueDate", Long.MIN_VALUE);
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithMaxLongValue() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Max Value Card");
        cardObj.put("dueDate", Long.MAX_VALUE);
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithZeroLengthCardName() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "");
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithSingleCharacterCardName() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "A");
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithExactly255CharacterCardName() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();

        StringBuilder name255 = new StringBuilder();
        for (int i = 0; i < 255; i++) {
            name255.append("A");
        }

        cardObj.put("name", name255.toString());
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithExactCurrentTime() throws Exception {
        long currentTime = System.currentTimeMillis();

        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Current Time Card");
        cardObj.put("dueDate", currentTime);
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithOneMillisecondDifference() throws Exception {
        long currentTime = System.currentTimeMillis();

        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "One MS Card");
        cardObj.put("dueDate", currentTime + 1);
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithExactlyMaxIntDays() throws Exception {
        long farFuture = System.currentTimeMillis() +
                TimeUnit.DAYS.toMillis(Integer.MAX_VALUE);

        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Far Future Card");
        cardObj.put("dueDate", farFuture);
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }
}