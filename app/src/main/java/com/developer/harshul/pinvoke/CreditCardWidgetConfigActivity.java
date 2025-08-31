package com.developer.harshul.pinvoke;

import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class CreditCardWidgetConfigActivity extends AppCompatActivity {

    private static final String TAG = "WidgetConfig";
    private static final String PREFS_NAME = "CCWidgetPrefs";
    private static final String CARDS_DATA_KEY = "cards_data";
    private static final int MAX_CARDS = 10; // Prevent memory issues
    private static final int MIN_CARDS = 1;

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private LinearLayout cardsContainer;
    private FloatingActionButton addCardFab;
    private MaterialToolbar toolbar;
    private Button saveButton;
    private List<CardEntry> cardEntries;
    private boolean isConfiguring = false; // Prevent multiple simultaneous saves

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_widget_config);
            initializeViews();
            setupWidget();
            loadExistingData();
            setupEventListeners();
            setResult(RESULT_CANCELED);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            showErrorAndFinish("Failed to initialize widget configuration");
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        cardsContainer = findViewById(R.id.cards_container);
        addCardFab = findViewById(R.id.add_card_button);
        saveButton = findViewById(R.id.save_button);
        cardEntries = new ArrayList<>();

        // Setup toolbar with null checks
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void setupWidget() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            }
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e(TAG, "Invalid widget ID");
            finish();
        }
    }

    private void setupEventListeners() {
        if (addCardFab != null) {
            addCardFab.setOnClickListener(v -> {
                try {
                    addNewCardEntry();
                } catch (Exception e) {
                    Log.e(TAG, "Error adding new card", e);
                    showToast("Failed to add new card");
                }
            });
        }

        if (saveButton != null) {
            saveButton.setOnClickListener(v -> {
                try {
                    saveConfiguration();
                } catch (Exception e) {
                    Log.e(TAG, "Error saving configuration", e);
                    showToast("Failed to save configuration");
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadExistingData() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
            String cardsDataJson = prefs.getString(CARDS_DATA_KEY, "");

            if (!TextUtils.isEmpty(cardsDataJson)) {
                parseAndLoadCards(cardsDataJson);
            } else {
                addDefaultCard();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading existing data", e);
            addDefaultCard();
        }
    }

    private void parseAndLoadCards(String cardsDataJson) {
        try {
            JSONArray cardsArray = new JSONArray(cardsDataJson);
            boolean hasValidCard = false;

            for (int i = 0; i < Math.min(cardsArray.length(), MAX_CARDS); i++) {
                JSONObject cardObj = cardsArray.optJSONObject(i);
                if (cardObj != null) {
                    String cardName = cardObj.optString("name", "");
                    long dueDate = cardObj.optLong("dueDate", getDefaultDueDate());

                    // Validate due date
                    if (isValidDate(dueDate)) {
                        addCardEntry(cardName, dueDate);
                        hasValidCard = true;
                    }
                }
            }

            if (!hasValidCard) {
                addDefaultCard();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing cards data", e);
            addDefaultCard();
        }
    }

    private boolean isValidDate(long dateMillis) {
        // Check if date is within reasonable bounds (not too far in past/future)
        long currentTime = System.currentTimeMillis();
        long fiveYearsAgo = currentTime - TimeUnit.DAYS.toMillis(5 * 365);
        long fiveYearsFromNow = currentTime + TimeUnit.DAYS.toMillis(5 * 365);

        return dateMillis >= fiveYearsAgo && dateMillis <= fiveYearsFromNow;
    }

    private void addDefaultCard() {
        try {
            Calendar defaultDate = Calendar.getInstance();
            defaultDate.add(Calendar.MONTH, 1);
            defaultDate.set(Calendar.DAY_OF_MONTH, 15);
            addCardEntry("", defaultDate.getTimeInMillis());
        } catch (Exception e) {
            Log.e(TAG, "Error adding default card", e);
        }
    }

    private void addNewCardEntry() {
        if (cardEntries.size() >= MAX_CARDS) {
            showToast("Maximum " + MAX_CARDS + " cards allowed");
            return;
        }

        try {
            Calendar defaultDate = Calendar.getInstance();
            defaultDate.add(Calendar.MONTH, 1);
            defaultDate.set(Calendar.DAY_OF_MONTH, 15);
            addCardEntry("", defaultDate.getTimeInMillis());
        } catch (Exception e) {
            Log.e(TAG, "Error in addNewCardEntry", e);
            showToast("Failed to add new card");
        }
    }

    private void addCardEntry(String cardName, long dueDate) {
        try {
            if (cardsContainer == null) {
                Log.e(TAG, "Cards container is null");
                return;
            }

            View cardView = LayoutInflater.from(this).inflate(R.layout.card_entry_item, cardsContainer, false);
            if (cardView == null) {
                Log.e(TAG, "Failed to inflate card view");
                return;
            }

            TextInputEditText cardNameEdit = cardView.findViewById(R.id.card_name_edit);
            Button dueDateButton = cardView.findViewById(R.id.due_date_button);
            ImageButton removeButton = cardView.findViewById(R.id.remove_card_button);

            if (cardNameEdit == null || dueDateButton == null || removeButton == null) {
                Log.e(TAG, "Failed to find required views in card entry");
                return;
            }

            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(dueDate);

            CardEntry cardEntry = new CardEntry(cardView, cardNameEdit, dueDateButton, selectedDate);
            cardEntries.add(cardEntry);

            // Set initial values safely
            cardNameEdit.setText(cardName != null ? cardName : "");
            updateDateButton(cardEntry);

            // Set up listeners with error handling
            dueDateButton.setOnClickListener(v -> {
                try {
                    showDatePicker(cardEntry);
                } catch (Exception e) {
                    Log.e(TAG, "Error showing date picker", e);
                    showToast("Failed to open date picker");
                }
            });

            removeButton.setOnClickListener(v -> {
                try {
                    removeCardEntry(cardEntry);
                } catch (Exception e) {
                    Log.e(TAG, "Error removing card", e);
                    showToast("Failed to remove card");
                }
            });

            cardsContainer.addView(cardView);
            updateRemoveButtonsVisibility();

        } catch (Exception e) {
            Log.e(TAG, "Error adding card entry", e);
        }
    }

    private void removeCardEntry(CardEntry cardEntry) {
        if (cardEntries.size() <= MIN_CARDS) {
            showToast("At least one card is required");
            return;
        }

        try {
            if (cardsContainer != null && cardEntry.cardView != null) {
                cardsContainer.removeView(cardEntry.cardView);
            }
            cardEntries.remove(cardEntry);
            updateRemoveButtonsVisibility();
        } catch (Exception e) {
            Log.e(TAG, "Error in removeCardEntry", e);
        }
    }

    private void updateRemoveButtonsVisibility() {
        try {
            boolean showRemoveButtons = cardEntries.size() > MIN_CARDS;
            for (CardEntry entry : cardEntries) {
                if (entry.cardView != null) {
                    ImageButton removeButton = entry.cardView.findViewById(R.id.remove_card_button);
                    if (removeButton != null) {
                        removeButton.setVisibility(showRemoveButtons ? View.VISIBLE : View.GONE);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating remove buttons visibility", e);
        }
    }

    private void showDatePicker(CardEntry cardEntry) {
        if (cardEntry == null || cardEntry.selectedDate == null) {
            Log.w(TAG, "Invalid card entry for date picker");
            return;
        }

        try {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (DatePicker view, int year, int month, int dayOfMonth) -> {
                        try {
                            cardEntry.selectedDate.set(Calendar.YEAR, year);
                            cardEntry.selectedDate.set(Calendar.MONTH, month);
                            cardEntry.selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            updateDateButton(cardEntry);
                        } catch (Exception e) {
                            Log.e(TAG, "Error setting date", e);
                            showToast("Failed to set date");
                        }
                    },
                    cardEntry.selectedDate.get(Calendar.YEAR),
                    cardEntry.selectedDate.get(Calendar.MONTH),
                    cardEntry.selectedDate.get(Calendar.DAY_OF_MONTH)
            );

            // Set min date to today to prevent past dates
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing date picker", e);
            showToast("Failed to open date picker");
        }
    }

    private void updateDateButton(CardEntry cardEntry) {
        if (cardEntry == null || cardEntry.dueDateButton == null || cardEntry.selectedDate == null) {
            return;
        }

        try {
            String dateStr = android.text.format.DateFormat.getDateFormat(this)
                    .format(cardEntry.selectedDate.getTime());
            cardEntry.dueDateButton.setText("Due: " + dateStr);
        } catch (Exception e) {
            Log.w(TAG, "Error updating date button", e);
            cardEntry.dueDateButton.setText("Due: Invalid Date");
        }
    }

    private void saveConfiguration() {
        if (isConfiguring) {
            return; // Prevent multiple saves
        }

        isConfiguring = true;

        try {
            if (cardEntries.isEmpty()) {
                showToast("Please add at least one card");
                return;
            }

            JSONArray cardsArray = new JSONArray();
            boolean hasValidCard = false;

            for (CardEntry entry : cardEntries) {
                if (entry.cardNameEdit == null || entry.selectedDate == null) {
                    continue;
                }

                String cardName = entry.cardNameEdit.getText() != null ?
                        entry.cardNameEdit.getText().toString().trim() : "";

                if (TextUtils.isEmpty(cardName)) {
                    cardName = "Credit Card";
                }

                try {
                    JSONObject cardObj = new JSONObject();
                    cardObj.put("name", cardName);
                    cardObj.put("dueDate", entry.selectedDate.getTimeInMillis());
                    cardsArray.put(cardObj);
                    hasValidCard = true;
                } catch (JSONException e) {
                    Log.w(TAG, "Error creating card JSON", e);
                }
            }

            if (!hasValidCard) {
                showToast("Please configure at least one valid card");
                return;
            }

            // Save in background to prevent ANR
            saveDataAsync(cardsArray);

        } catch (Exception e) {
            Log.e(TAG, "Error in saveConfiguration", e);
            showToast("Failed to save configuration");
        } finally {
            isConfiguring = false;
        }
    }

    private void saveDataAsync(JSONArray cardsArray) {
        // Disable save button during operation
        if (saveButton != null) {
            saveButton.setEnabled(false);
            saveButton.setText("Saving...");
        }

        new Thread(() -> {
            try {
                // Save preferences
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME + appWidgetId, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(CARDS_DATA_KEY, cardsArray.toString());

                if (editor.commit()) { // Use commit for synchronous save
                    // Update widget on main thread
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(() -> {
                        try {
                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                            if (appWidgetManager != null) {
                                CreditCardWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);
                            }

                            // Set result and finish
                            Intent resultValue = new Intent();
                            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                            setResult(RESULT_OK, resultValue);
                            finish();

                        } catch (Exception e) {
                            Log.e(TAG, "Error updating widget after save", e);
                            showToast("Configuration saved but widget update failed");
                            finish();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        showToast("Failed to save configuration");
                        if (saveButton != null) {
                            saveButton.setEnabled(true);
                            saveButton.setText("Save Widget");
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in background save", e);
                runOnUiThread(() -> {
                    showToast("Failed to save configuration");
                    if (saveButton != null) {
                        saveButton.setEnabled(true);
                        saveButton.setText("Save Widget");
                    }
                });
            }
        }).start();
    }

    private long getDefaultDueDate() {
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

    private void showToast(String message) {
        try {
            if (!isFinishing() && !isDestroyed()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast", e);
        }
    }

    private void showErrorAndFinish(String message) {
        showToast(message);
        finish();
    }

    @Override
    protected void onDestroy() {
        try {
            // Clear references to prevent memory leaks
            if (cardEntries != null) {
                cardEntries.clear();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
        super.onDestroy();
    }

    // Enhanced CardEntry class with null safety
    private static class CardEntry {
        final View cardView;
        final TextInputEditText cardNameEdit;
        final Button dueDateButton;
        final Calendar selectedDate;

        CardEntry(View cardView, TextInputEditText cardNameEdit, Button dueDateButton, Calendar selectedDate) {
            this.cardView = cardView;
            this.cardNameEdit = cardNameEdit;
            this.dueDateButton = dueDateButton;
            this.selectedDate = selectedDate != null ? selectedDate : Calendar.getInstance();
        }

        boolean isValid() {
            return cardView != null && cardNameEdit != null &&
                    dueDateButton != null && selectedDate != null;
        }
    }
}