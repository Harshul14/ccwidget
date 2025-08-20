package com.developer.harshul.ccwidget;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class CreditCardWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "CreditCardWidget";
    private static final String PREFS_NAME = "CCWidgetPrefs";
    private static final String CARDS_DATA_KEY = "cards_data";
    private static final String ACTION_WIDGET_CLICKED = "ACTION_WIDGET_CLICKED";

    // Cache for date formatter to avoid repeated creation
    private static SimpleDateFormat dateFormatter;

    static {
        dateFormatter = new SimpleDateFormat("MMM dd", Locale.getDefault());
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Process updates in background to prevent ANR
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            for (int appWidgetId : appWidgetIds) {
                try {
                    updateAppWidget(context, appWidgetManager, appWidgetId);
                } catch (Exception e) {
                    Log.e(TAG, "Error updating widget " + appWidgetId, e);
                    // Show error state instead of crashing
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

            // Get saved data with null checks
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
            String cardsDataJson = prefs.getString(CARDS_DATA_KEY, "");

            CardData cardData = parseCardData(cardsDataJson);
            updateWidgetDisplay(views, cardData);
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
        long currentTime = System.currentTimeMillis();
        int totalCards = cardsArray.length();

        // First pass: Look for future dates or today
        for (int i = 0; i < cardsArray.length(); i++) {
            JSONObject cardObj = cardsArray.getJSONObject(i);
            String cardName = cardObj.optString("name", "Credit Card");
            long dueDate = cardObj.optLong("dueDate", getDefaultDueDate());

            if (dueDate >= currentTime - TimeUnit.DAYS.toMillis(1)) {
                if (dueDate < nearestDueDate) {
                    nearestDueDate = dueDate;
                    nearestCardName = cardName;
                }
            }
        }

        // Second pass: If no future dates, find most recent overdue
        if (nearestDueDate == Long.MAX_VALUE) {
            nearestDueDate = Long.MIN_VALUE;
            for (int i = 0; i < cardsArray.length(); i++) {
                JSONObject cardObj = cardsArray.getJSONObject(i);
                String cardName = cardObj.optString("name", "Credit Card");
                long dueDate = cardObj.optLong("dueDate", getDefaultDueDate());

                if (dueDate > nearestDueDate) {
                    nearestDueDate = dueDate;
                    nearestCardName = cardName;
                }
            }
        }

        return new CardData(nearestCardName, nearestDueDate, totalCards);
    }

    private static CardData getDefaultCardData() {
        return new CardData("Credit Card", getDefaultDueDate(), 1);
    }

    private static void updateWidgetDisplay(RemoteViews views, CardData cardData) {
        try {
            // Calculate days remaining
            long currentTime = System.currentTimeMillis();
            long diffMillis = cardData.dueDate - currentTime;
            int daysRemaining = (int) TimeUnit.MILLISECONDS.toDays(diffMillis);

            // Format the display safely
            String dueDateStr;
            try {
                dueDateStr = dateFormatter.format(new Date(cardData.dueDate));
            } catch (Exception e) {
                dueDateStr = "Invalid Date";
                Log.w(TAG, "Error formatting date", e);
            }

            // Update widget views with null checks
            String displayName = cardData.name;
            if (cardData.totalCards > 1) {
                displayName = cardData.name + " (+" + (cardData.totalCards - 1) + " more)";
            }

            views.setTextViewText(R.id.card_name, displayName != null ? displayName : "Credit Card");
            views.setTextViewText(R.id.due_date, dueDateStr);

            // Set days remaining text and color with bounds checking
            DaysDisplayInfo displayInfo = calculateDaysDisplay(daysRemaining);
            views.setTextViewText(R.id.days_remaining, displayInfo.text);
            views.setTextColor(R.id.days_remaining, displayInfo.color);

        } catch (Exception e) {
            Log.e(TAG, "Error updating widget display", e);
            // Set safe default values
            views.setTextViewText(R.id.card_name, "Credit Card");
            views.setTextViewText(R.id.due_date, "---");
            views.setTextViewText(R.id.days_remaining, "--");
        }
    }

    private static DaysDisplayInfo calculateDaysDisplay(int daysRemaining) {
        String daysText;
        int textColor;

        try {
            if (daysRemaining < 0) {
                daysText = "OVERDUE";
                textColor = android.graphics.Color.parseColor("#F44336"); // Material Red
            } else if (daysRemaining == 0) {
                daysText = "TODAY";
                textColor = android.graphics.Color.parseColor("#F44336"); // Material Red
            } else if (daysRemaining <= 3) {
                daysText = String.valueOf(daysRemaining);
                textColor = android.graphics.Color.parseColor("#FF9800"); // Material Orange
            } else if (daysRemaining <= 7) {
                daysText = String.valueOf(daysRemaining);
                textColor = android.graphics.Color.parseColor("#FFC107"); // Material Amber
            } else {
                daysText = String.valueOf(daysRemaining);
                textColor = android.graphics.Color.parseColor("#4CAF50"); // Material Green
            }
        } catch (Exception e) {
            Log.w(TAG, "Error calculating days display", e);
            daysText = "--";
            textColor = android.graphics.Color.GRAY;
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
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_credit_card);
            views.setTextViewText(R.id.card_name, "Error");
            views.setTextViewText(R.id.due_date, "Tap to retry");
            views.setTextViewText(R.id.days_remaining, "!");
            views.setTextColor(R.id.days_remaining, android.graphics.Color.RED);

            setupClickIntent(context, views, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (Exception e) {
            Log.e(TAG, "Error showing error state", e);
        }
    }

    private static long getDefaultDueDate() {
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 15);
            return cal.getTimeInMillis();
        } catch (Exception e) {
            Log.w(TAG, "Error getting default due date", e);
            return System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        try {
            // Clean up preferences when widget is deleted
            for (int appWidgetId : appWidgetIds) {
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
                prefs.edit().clear().apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up deleted widgets", e);
        }
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            super.onReceive(context, intent);
        } catch (Exception e) {
            Log.e(TAG, "Error in onReceive", e);
        }
    }

    // Data classes for better organization
    private static class CardData {
        final String name;
        final long dueDate;
        final int totalCards;

        CardData(String name, long dueDate, int totalCards) {
            this.name = name != null ? name : "Credit Card";
            this.dueDate = dueDate;
            this.totalCards = Math.max(totalCards, 1);
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