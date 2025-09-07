package com.developer.harshul.pinvoke;

import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class MainActivityTest {

    private MainActivity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(MainActivity.class)
                .create()
                .start()
                .resume()
                .get();
    }

    @Test
    public void testActivityCreation() {
        assertNotNull(activity);
        assertFalse(activity.isFinishing());
    }

    @Test
    public void testAddWidgetButtonClick() {
        // Given
        MaterialButton addWidgetButton = activity.findViewById(R.id.add_widget_button);
        assertNotNull(addWidgetButton);

        // When
        addWidgetButton.performClick();

        // Then
        Toast latestToast = ShadowToast.getLatestToast();
        assertNotNull(latestToast);

        String toastText = ShadowToast.getTextOfLatestToast();
        assertTrue(toastText.contains("Widget"));
    }

    @Test
    public void testUIElementsPresent() {
        // Test that required UI elements are present
        assertNotNull(activity.findViewById(R.id.add_widget_button));
        assertNotNull(activity.findViewById(R.id.info_card));
    }
}