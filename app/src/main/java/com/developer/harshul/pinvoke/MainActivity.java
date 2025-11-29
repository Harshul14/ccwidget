package com.developer.harshul.pinvoke;

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
        MaterialButton addWidgetButton = findViewById(R.id.add_widget_button);
        if (addWidgetButton != null) {
            addWidgetButton.setOnClickListener(v -> openWidgetPicker());
        }
    }

    private void openWidgetPicker() {
        showToast(getString(R.string.long_press_on_home_screen_and_select_widgets));
    }

    private void showToast(String message) {
        if (!isFinishing() && !isDestroyed()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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