package com.developer.harshul.ccwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private static final String PREFS_NAME = "CCWidgetPrefs";
    private static final String CARDS_DATA_KEY = "cards_data";
    private static final String ACTION_WIDGET_CLICKED = "ACTION_WIDGET_CLICKED";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_credit_card);

        // Get saved data
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
        String cardsDataJson = prefs.getString(CARDS_DATA_KEY, "");

        if (!cardsDataJson.isEmpty()) {
            try {
                JSONArray cardsArray = new JSONArray(cardsDataJson);
                if (cardsArray.length() > 0) {
                    // Find the card with the nearest due date
                    String nearestCardName = "";
                    long nearestDueDate = Long.MAX_VALUE;
                    long currentTime = System.currentTimeMillis();

                    for (int i = 0; i < cardsArray.length(); i++) {
                        JSONObject cardObj = cardsArray.getJSONObject(i);
                        String cardName = cardObj.getString("name");
                        long dueDate = cardObj.getLong("dueDate");

                        // Only consider future dates or today, unless all are overdue
                        if (dueDate >= currentTime - TimeUnit.DAYS.toMillis(1)) {
                            if (dueDate < nearestDueDate) {
                                nearestDueDate = dueDate;
                                nearestCardName = cardName;
                            }
                        }
                    }

                    // If no future dates found, show the most recently overdue
                    if (nearestDueDate == Long.MAX_VALUE) {
                        for (int i = 0; i < cardsArray.length(); i++) {
                            JSONObject cardObj = cardsArray.getJSONObject(i);
                            String cardName = cardObj.getString("name");
                            long dueDate = cardObj.getLong("dueDate");

                            if (dueDate > nearestDueDate || nearestDueDate == Long.MAX_VALUE) {
                                nearestDueDate = dueDate;
                                nearestCardName = cardName;
                            }
                        }
                    }

                    updateWidgetDisplay(views, nearestCardName, nearestDueDate, cardsArray.length());
                } else {
                    // No cards found, show default
                    updateWidgetDisplay(views, "Credit Card", getDefaultDueDate(), 1);
                }
            } catch (JSONException e) {
                // If parsing fails, show default
                updateWidgetDisplay(views, "Credit Card", getDefaultDueDate(), 1);
            }
        } else {
            // No data found, show default
            updateWidgetDisplay(views, "Credit Card", getDefaultDueDate(), 1);
        }

        // Set up click intent to open configuration
        Intent intent = new Intent(context, CreditCardWidgetConfigActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void updateWidgetDisplay(RemoteViews views, String cardName, long dueDateMillis, int totalCards) {
        // Calculate days remaining
        long currentTime = System.currentTimeMillis();
        long diffMillis = dueDateMillis - currentTime;
        int daysRemaining = (int) TimeUnit.MILLISECONDS.toDays(diffMillis);

        // Format the display
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        String dueDateStr = dateFormat.format(new Date(dueDateMillis));

        // Update widget views
        String displayName = cardName;
        if (totalCards > 1) {
            displayName = cardName + " (+" + (totalCards - 1) + " more)";
        }
        views.setTextViewText(R.id.card_name, displayName);
        views.setTextViewText(R.id.due_date, dueDateStr);

        // Set days remaining text and color
        String daysText;
        int textColor;
        if (daysRemaining < 0) {
            daysText = "OVERDUE";
            textColor = android.graphics.Color.RED;
        } else if (daysRemaining == 0) {
            daysText = "DUE TODAY";
            textColor = android.graphics.Color.RED;
        } else if (daysRemaining <= 3) {
            daysText = daysRemaining + " days";
            textColor = android.graphics.Color.parseColor("#FF9800"); // Orange
        } else {
            daysText = daysRemaining + " days";
            textColor = android.graphics.Color.parseColor("#4CAF50"); // Green
        }

        views.setTextViewText(R.id.days_remaining, daysText);
        views.setTextColor(R.id.days_remaining, textColor);
    }

    private static long getDefaultDueDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        return cal.getTimeInMillis();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Clean up preferences when widget is deleted
        for (int appWidgetId : appWidgetIds) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        }
    }
}