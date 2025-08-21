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
public class LocalizationTests {

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
    public void testWidget_WithChineseCharacters() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "信用卡");
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithArabicCharacters() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "بطاقة ائتمان");
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithJapaneseCharacters() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "クレジットカード");
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithRussianCharacters() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Кредитная карта");
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithMixedLanguageCharacters() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Card信用卡بطاقةクレジット");
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }

    @Test
    public void testWidget_WithRightToLeftText() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "כרטיס אשראי"); // Hebrew
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }
}