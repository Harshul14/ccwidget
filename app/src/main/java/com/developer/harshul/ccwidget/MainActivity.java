package com.developer.harshul.ccwidget;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);
            setupViews();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            showErrorAndFinish("Failed to initialize app");
        }
    }

    private void setupViews() {
        try {
            MaterialButton addWidgetButton = findViewById(R.id.add_widget_button);
            MaterialCardView infoCard = findViewById(R.id.info_card);

            if (addWidgetButton != null) {
                addWidgetButton.setOnClickListener(v -> {
                    try {
                        openWidgetPicker();
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening widget picker", e);
                        showToast("Failed to open widget picker");
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up views", e);
        }
    }

    private void openWidgetPicker() {
        try {
            // This will guide user to add widget through launcher
            showToast("Long press on home screen and select 'Widgets' to add Credit Card Widget");
        } catch (Exception e) {
            Log.e(TAG, "Error in openWidgetPicker", e);
        }
    }

    private void showToast(String message) {
        try {
            if (!isFinishing() && !isDestroyed()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
            // Cleanup if needed
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy", e);
        }
        super.onDestroy();
    }
}