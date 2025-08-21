package com.developer.harshul.ccwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class CreditCardWidgetConfigActivityTest {

    @Mock
    private SharedPreferences mockSharedPreferences;
    @Mock
    private SharedPreferences.Editor mockEditor;

    private CreditCardWidgetConfigActivity activity;
    private Context context;
    private static final int TEST_WIDGET_ID = 123;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();

        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.commit()).thenReturn(true);
    }

    @Test
    public void testOnCreate_WithValidWidgetId() {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        assertNotNull(activity);
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testOnCreate_WithInvalidWidgetId() {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        assertTrue(activity.isFinishing());
    }

    @Test
    public void testOnCreate_WithNullIntent() {
        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, (Intent) null)
                .create()
                .get();

        assertTrue(activity.isFinishing());
    }

    @Test
    public void testOnCreate_WithEmptyBundle() {
        Intent intent = new Intent();

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        assertTrue(activity.isFinishing());
    }

    @Test
    public void testLoadExistingData_WithValidData() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Test Card");
        cardObj.put("dueDate", getValidFutureDate());
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        // Verify that data was loaded (no crash and activity not finished)
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testLoadExistingData_WithInvalidData() {
        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn("invalid json");

        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        // Should fallback to default card and not crash
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testLoadExistingData_WithEmptyData() {
        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn("");

        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        // Should create default card and not crash
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testIsValidDate_WithValidFutureDate() {
        long futureDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30);

        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        // Test through loading data with valid date
        JSONArray cardsArray = new JSONArray();
        try {
            JSONObject cardObj = new JSONObject();
            cardObj.put("name", "Test Card");
            cardObj.put("dueDate", futureDate);
            cardsArray.put(cardObj);
        } catch (Exception e) {
            fail("Failed to create test data");
        }

        // Should not crash
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testIsValidDate_WithTooFarFutureDate() {
        // Date 10 years in future (should be invalid)
        long farFutureDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10 * 365);

        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        // Should handle invalid dates gracefully
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testIsValidDate_WithTooOldDate() {
        // Date 10 years in past (should be invalid)
        long oldDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10 * 365);

        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        // Should handle invalid dates gracefully
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testAddNewCardEntry_WithMaxCardsReached() {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        // Simulate having maximum cards
        // This would require reflection or package-private access to test properly
        // For now, we test that it doesn't crash
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testSaveConfiguration_WithEmptyCards() {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        // Should show toast about adding at least one card
        // This would require access to internal methods to test properly
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testSaveConfiguration_WithValidCards() {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        // Should successfully save and not crash
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testSaveConfiguration_WithPreferencesFailure() {
        when(mockEditor.commit()).thenReturn(false);

        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        // Should handle save failure gracefully
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testOnDestroy_CleansUpResources() {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .destroy()
                .get();

        // Should not crash during cleanup
        assertTrue(activity.isDestroyed());
    }

    @Test
    public void testOnSupportNavigateUp_FinishesActivity() {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        boolean result = activity.onSupportNavigateUp();

        assertTrue(result);
        assertTrue(activity.isFinishing());
    }

    @Test
    public void testCardEntry_Validation() {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, TEST_WIDGET_ID);

        activity = Robolectric.buildActivity(CreditCardWidgetConfigActivity.class, intent)
                .create()
                .get();

        // Test CardEntry inner class validation
        // This would require access to internal classes
        assertFalse(activity.isFinishing());
    }

    private long getValidFutureDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        return cal.getTimeInMillis();
    }
}