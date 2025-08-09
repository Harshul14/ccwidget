package com.developer.harshul.ccwidget;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import java.util.Calendar;

public class CreditCardWidgetConfigActivity extends Activity {

    private static final String PREFS_NAME = "CCWidgetPrefs";
    private static final String CARD_NAME_KEY = "card_name";
    private static final String DUE_DATE_KEY = "due_date";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText cardNameEdit;
    private Button dueDateButton;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);

        // Initialize views
        cardNameEdit = findViewById(R.id.card_name_edit);
        dueDateButton = findViewById(R.id.due_date_button);
        Button saveButton = findViewById(R.id.save_button);

        // Get widget ID
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // Initialize selected date to next month
        selectedDate = Calendar.getInstance();
        selectedDate.add(Calendar.MONTH, 1);
        selectedDate.set(Calendar.DAY_OF_MONTH, 15);
        updateDateButton();

        // Load existing data if available
        loadExistingData();

        // Set up date picker
        dueDateButton.setOnClickListener(v -> showDatePicker());

        // Set up save button
        saveButton.setOnClickListener(v -> saveConfiguration());

        // Set result to CANCELED initially
        setResult(RESULT_CANCELED);
    }

    private void loadExistingData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
        String cardName = prefs.getString(CARD_NAME_KEY, "");
        long dueDateMillis = prefs.getLong(DUE_DATE_KEY, 0);

        if (!cardName.isEmpty()) {
            cardNameEdit.setText(cardName);
        }

        if (dueDateMillis != 0) {
            selectedDate.setTimeInMillis(dueDateMillis);
            updateDateButton();
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateButton();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateButton() {
        String dateStr = android.text.format.DateFormat.getDateFormat(this).format(selectedDate.getTime());
        dueDateButton.setText("Due Date: " + dateStr);
    }

    private void saveConfiguration() {
        String cardName = cardNameEdit.getText().toString().trim();
        if (cardName.isEmpty()) {
            cardName = "Credit Card";
        }

        // Save preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CARD_NAME_KEY, cardName);
        editor.putLong(DUE_DATE_KEY, selectedDate.getTimeInMillis());
        editor.apply();

        // Update widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        CreditCardWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);

        // Set result and finish
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}