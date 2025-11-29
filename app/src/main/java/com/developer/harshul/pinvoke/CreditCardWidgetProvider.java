package com.developer.harshul.pinvoke;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CreditCardWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "CreditCardWidget";
    private static final String PREFS_NAME = "CCWidgetPrefs";
    private static final String CARDS_DATA_KEY = "cards_data";
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        executorService.execute(() -> {
            for (int appWidgetId : appWidgetIds) {
                try {
                    updateAppWidget(context, appWidgetManager, appWidgetId);
                } catch (Exception e) {
                    Log.e(TAG, "Error updating widget " + appWidgetId, e);
                    showErrorState(context, appWidgetManager, appWidgetId);
                }
            }
        });
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        if (context == null || appWidgetManager == null) {
            Log.w(TAG, "Context or AppWidgetManager is null");
            return;
        }

        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_credit_card);

            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
            String cardsDataJson = prefs.getString(CARDS_DATA_KEY, "");

            CardData cardData = parseCardData(cardsDataJson);

            if (cardData.updatedData != null) {
                saveUpdatedData(context, appWidgetId, cardData.updatedData);
            }

            updateWidgetDisplay(context, views, cardData);
            setupClickIntent(context, views, appWidgetId);

            appWidgetManager.updateAppWidget(appWidgetId, views);

        } catch (Exception e) {
            Log.e(TAG, "Error in updateAppWidget", e);
            showErrorState(context, appWidgetManager, appWidgetId);
        }
    }

    private static CardData parseCardData(String cardsDataJson) {
        if (cardsDataJson == null || cardsDataJson.trim().isEmpty()) {
            return getDefaultCardData();
        }

        try {
            JSONArray cardsArray = new JSONArray(cardsDataJson);
            if (cardsArray.length() == 0) {
                return getDefaultCardData();
            }

            return findNearestCard(cardsArray);

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing card data", e);
            return getDefaultCardData();
        }
    }

    private static CardData findNearestCard(JSONArray cardsArray) throws JSONException {
        String nearestCardName = "";
        long nearestDueDate = Long.MAX_VALUE;
        boolean dataUpdated = false;
        JSONArray updatedCardsArray = new JSONArray();

        for (int i = 0; i < cardsArray.length(); i++) {
            JSONObject cardObj = cardsArray.getJSONObject(i);
            String cardName = cardObj.optString("name", "Credit Card");
            long originalDueDate = cardObj.optLong("dueDate", getDefaultDueDate());
            long updatedDueDate = updateOverdueDateToNextMonth(originalDueDate);

            if (updatedDueDate != originalDueDate) {
                dataUpdated = true;
            }

            JSONObject updatedCardObj = new JSONObject();
            updatedCardObj.put("name", cardName);
            updatedCardObj.put("dueDate", updatedDueDate);
            updatedCardsArray.put(updatedCardObj);

            if (updatedDueDate < nearestDueDate) {
                nearestDueDate = updatedDueDate;
                nearestCardName = cardName;
            }
        }

        return new CardData(nearestCardName, nearestDueDate, cardsArray.length(), dataUpdated ? updatedCardsArray : null);
    }

    private static CardData getDefaultCardData() {
        return new CardData("Credit Card", getDefaultDueDate(), 1);
    }

    private static void updateWidgetDisplay(Context context, RemoteViews views, CardData cardData) {
        try {
            long currentTime = System.currentTimeMillis();
            long diffMillis = cardData.dueDate - currentTime;
            int daysRemaining = (int) TimeUnit.MILLISECONDS.toDays(diffMillis);

            SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd", Locale.getDefault());
            String dueDateStr = dateFormatter.format(new Date(cardData.dueDate));

            String displayName = cardData.name;
            if (cardData.totalCards > 1) {
                displayName = cardData.name + " (+" + (cardData.totalCards - 1) + " more)";
            }

            views.setTextViewText(R.id.card_name, displayName != null ? displayName : "Credit Card");
            views.setTextViewText(R.id.due_date, dueDateStr);

            DaysDisplayInfo displayInfo = calculateDaysDisplay(context, daysRemaining);
            views.setTextViewText(R.id.days_remaining, displayInfo.text);
            views.setTextColor(R.id.days_remaining, displayInfo.color);

        } catch (Exception e) {
            Log.e(TAG, "Error updating widget display", e);
            views.setTextViewText(R.id.card_name, "Credit Card");
            views.setTextViewText(R.id.due_date, "---");
            views.setTextViewText(R.id.days_remaining, "--");
        }
    }

    private static DaysDisplayInfo calculateDaysDisplay(Context context, int daysRemaining) {
        String daysText;
        int textColor;

        if (daysRemaining < 0) {
            daysText = "OVERDUE";
            textColor = ContextCompat.getColor(context, R.color.error);
        } else if (daysRemaining == 0) {
            daysText = "TODAY";
            textColor = ContextCompat.getColor(context, R.color.error);
        } else if (daysRemaining <= 3) {
            daysText = String.valueOf(daysRemaining);
            textColor = ContextCompat.getColor(context, R.color.warning);
        } else if (daysRemaining <= 7) {
            daysText = String.valueOf(daysRemaining);
            textColor = ContextCompat.getColor(context, R.color.amber);
        } else {
            daysText = String.valueOf(daysRemaining);
            textColor = ContextCompat.getColor(context, R.color.success);
        }

        return new DaysDisplayInfo(daysText, textColor);
    }

    private static void setupClickIntent(Context context, RemoteViews views, int appWidgetId) {
        try {
            Intent intent = new Intent(context, CreditCardWidgetConfigActivity.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, appWidgetId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click intent", e);
        }
    }

    private static void showErrorState(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_credit_card);
                views.setTextViewText(R.id.card_name, "Error");
                views.setTextViewText(R.id.due_date, "Tap to retry");
                views.setTextViewText(R.id.days_remaining, "!");
                views.setTextColor(R.id.days_remaining, ContextCompat.getColor(context, R.color.error));

                setupClickIntent(context, views, appWidgetId);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            } catch (Exception e) {
                Log.e(TAG, "Error showing error state", e);
            }
        });
    }

    private static long getDefaultDueDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        return cal.getTimeInMillis();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        }
        super.onDeleted(context, appWidgetIds);
    }

    private static long updateOverdueDateToNextMonth(long dueDate) {
        Calendar currentCal = Calendar.getInstance();
        Calendar dueCal = Calendar.getInstance();
        dueCal.setTimeInMillis(dueDate);

        currentCal.set(Calendar.HOUR_OF_DAY, 0);
        currentCal.set(Calendar.MINUTE, 0);
        currentCal.set(Calendar.SECOND, 0);
        currentCal.set(Calendar.MILLISECOND, 0);

        dueCal.set(Calendar.HOUR_OF_DAY, 0);
        dueCal.set(Calendar.MINUTE, 0);
        dueCal.set(Calendar.SECOND, 0);
        dueCal.set(Calendar.MILLISECOND, 0);

        if (dueCal.before(currentCal)) {
            int dayOfMonth = dueCal.get(Calendar.DAY_OF_MONTH);

            Calendar nextMonth = Calendar.getInstance();
            nextMonth.add(Calendar.MONTH, 1);
            nextMonth.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            nextMonth.set(Calendar.HOUR_OF_DAY, 0);
            nextMonth.set(Calendar.MINUTE, 0);
            nextMonth.set(Calendar.SECOND, 0);
            nextMonth.set(Calendar.MILLISECOND, 0);

            return nextMonth.getTimeInMillis();
        }

        return dueDate;
    }

    private static void saveUpdatedData(Context context, int appWidgetId, JSONArray updatedData) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
        prefs.edit().putString(CARDS_DATA_KEY, updatedData.toString()).apply();
        Log.d(TAG, "Updated overdue dates for widget " + appWidgetId);
    }

    private static class CardData {
        final String name;
        final long dueDate;
        final int totalCards;
        final JSONArray updatedData;

        CardData(String name, long dueDate, int totalCards) {
            this(name, dueDate, totalCards, null);
        }

        CardData(String name, long dueDate, int totalCards, JSONArray updatedData) {
            this.name = name != null ? name : "Credit Card";
            this.dueDate = dueDate;
            this.totalCards = Math.max(totalCards, 1);
            this.updatedData = updatedData;
        }
    }

    private static class DaysDisplayInfo {
        final String text;
        final int color;

        DaysDisplayInfo(String text, int color) {
            this.text = text != null ? text : "--";
            this.color = color;
        }
    }
}