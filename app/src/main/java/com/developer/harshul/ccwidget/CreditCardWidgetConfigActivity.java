package com.developer.harshul.ccwidget;

import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class CreditCardWidgetConfigActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "CCWidgetPrefs";
    private static final String CARDS_DATA_KEY = "cards_data";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private LinearLayout cardsContainer;
    private FloatingActionButton addCardFab;  // CHANGED FROM Button
    private MaterialToolbar toolbar;          // ADDED
    private List<CardEntry> cardEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_config);

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        cardsContainer = findViewById(R.id.cards_container);
        addCardFab = findViewById(R.id.add_card_button);  // CHANGED VARIABLE NAME
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

        // Initialize card entries list
        cardEntries = new ArrayList<>();

        // Load existing data or add default card
        loadExistingData();

        // Set up buttons - UPDATED FAB REFERENCE
        addCardFab.setOnClickListener(v -> addNewCardEntry());
        saveButton.setOnClickListener(v -> saveConfiguration());

        // Set result to CANCELED initially
        setResult(RESULT_CANCELED);
    }

    // ADD THIS METHOD FOR TOOLBAR BACK BUTTON
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadExistingData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
        String cardsDataJson = prefs.getString(CARDS_DATA_KEY, "");

        if (!cardsDataJson.isEmpty()) {
            try {
                JSONArray cardsArray = new JSONArray(cardsDataJson);
                for (int i = 0; i < cardsArray.length(); i++) {
                    JSONObject cardObj = cardsArray.getJSONObject(i);
                    String cardName = cardObj.getString("name");
                    long dueDate = cardObj.getLong("dueDate");
                    addCardEntry(cardName, dueDate);
                }
            } catch (JSONException e) {
                // If parsing fails, add default card
                addDefaultCard();
            }
        } else {
            // Add default card if no existing data
            addDefaultCard();
        }
    }

    private void addDefaultCard() {
        Calendar defaultDate = Calendar.getInstance();
        defaultDate.add(Calendar.MONTH, 1);
        defaultDate.set(Calendar.DAY_OF_MONTH, 15);
        addCardEntry("", defaultDate.getTimeInMillis());
    }

    private void addNewCardEntry() {
        Calendar defaultDate = Calendar.getInstance();
        defaultDate.add(Calendar.MONTH, 1);
        defaultDate.set(Calendar.DAY_OF_MONTH, 15);
        addCardEntry("", defaultDate.getTimeInMillis());
    }

    private void addCardEntry(String cardName, long dueDate) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.card_entry_item, cardsContainer, false);

        // UPDATED TO USE TextInputEditText
        TextInputEditText cardNameEdit = cardView.findViewById(R.id.card_name_edit);
        Button dueDateButton = cardView.findViewById(R.id.due_date_button);
        ImageButton removeButton = cardView.findViewById(R.id.remove_card_button);

        Calendar selectedDate = Calendar.getInstance();
        selectedDate.setTimeInMillis(dueDate);

        CardEntry cardEntry = new CardEntry(cardView, cardNameEdit, dueDateButton, selectedDate);
        cardEntries.add(cardEntry);

        // Set initial values
        cardNameEdit.setText(cardName);
        updateDateButton(cardEntry);

        // Set up date picker
        dueDateButton.setOnClickListener(v -> showDatePicker(cardEntry));

        // Set up remove button
        removeButton.setOnClickListener(v -> removeCardEntry(cardEntry));

        // Add view to container first
        cardsContainer.addView(cardView);

        // Then update remove buttons visibility
        updateRemoveButtonsVisibility();
    }

    private void removeCardEntry(CardEntry cardEntry) {
        cardsContainer.removeView(cardEntry.cardView);
        cardEntries.remove(cardEntry);
        updateRemoveButtonsVisibility();
    }

    private void updateRemoveButtonsVisibility() {
        boolean showRemoveButtons = cardEntries.size() > 1;
        for (CardEntry entry : cardEntries) {
            ImageButton removeButton = entry.cardView.findViewById(R.id.remove_card_button);
            removeButton.setVisibility(showRemoveButtons ? View.VISIBLE : View.GONE);
        }
    }

    private void showDatePicker(CardEntry cardEntry) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    cardEntry.selectedDate.set(Calendar.YEAR, year);
                    cardEntry.selectedDate.set(Calendar.MONTH, month);
                    cardEntry.selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateButton(cardEntry);
                },
                cardEntry.selectedDate.get(Calendar.YEAR),
                cardEntry.selectedDate.get(Calendar.MONTH),
                cardEntry.selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateButton(CardEntry cardEntry) {
        String dateStr = android.text.format.DateFormat.getDateFormat(this).format(cardEntry.selectedDate.getTime());
        cardEntry.dueDateButton.setText("Due: " + dateStr);
    }

    private void saveConfiguration() {
        JSONArray cardsArray = new JSONArray();

        for (CardEntry entry : cardEntries) {
            String cardName = entry.cardNameEdit.getText().toString().trim();
            if (cardName.isEmpty()) {
                cardName = "Credit Card";
            }

            try {
                JSONObject cardObj = new JSONObject();
                cardObj.put("name", cardName);
                cardObj.put("dueDate", entry.selectedDate.getTimeInMillis());
                cardsArray.put(cardObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Save preferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CARDS_DATA_KEY, cardsArray.toString());
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

    // UPDATED CardEntry CLASS
    private static class CardEntry {
        View cardView;
        TextInputEditText cardNameEdit;  // CHANGED FROM EditText
        Button dueDateButton;
        Calendar selectedDate;

        CardEntry(View cardView, TextInputEditText cardNameEdit, Button dueDateButton, Calendar selectedDate) {
            this.cardView = cardView;
            this.cardNameEdit = cardNameEdit;
            this.dueDateButton = dueDateButton;
            this.selectedDate = selectedDate;
        }
    }
}