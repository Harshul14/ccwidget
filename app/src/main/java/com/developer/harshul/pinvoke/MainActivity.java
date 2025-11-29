package com.developer.harshul.pinvoke;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
    }

    private void setupViews() {
        MaterialButton addWidgetButton = findViewById(R.id.add_widget_button);
        addWidgetButton.setOnClickListener(v -> openWidgetPicker());
    }

    private void openWidgetPicker() {
        Toast.makeText(this, R.string.long_press_on_home_screen_and_select_widgets, Toast.LENGTH_LONG).show();
    }
}