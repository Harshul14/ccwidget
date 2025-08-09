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

public class CreditCardWidgetProvider extends AppWidgetProvider {

    private static final String PREFS_NAME = "CCWidgetPrefs";
    private static final String CARD_NAME_KEY = "card_name";
    private static final String DUE_DATE_KEY = "due_date";
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
        String cardName = prefs.getString(CARD_NAME_KEY, "Credit Card");
        long dueDateMillis = prefs.getLong(DUE_DATE_KEY, 0);

        if (dueDateMillis == 0) {
            // Set default due date to next month's 15th
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 15);
            dueDateMillis = cal.getTimeInMillis();
        }

        // Calculate days remaining
        long currentTime = System.currentTimeMillis();
        long diffMillis = dueDateMillis - currentTime;
        int daysRemaining = (int) TimeUnit.MILLISECONDS.toDays(diffMillis);

        // Format the display
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        String dueDateStr = dateFormat.format(new Date(dueDateMillis));

        // Update widget views
        views.setTextViewText(R.id.card_name, cardName);
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

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Clean up preferences when widget is deleted
        for (int appWidgetId : appWidgetIds) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        }
    }
}