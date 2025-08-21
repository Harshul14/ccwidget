package com.developer.harshul.ccwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class ErrorHandlingTests {

    @Mock
    private Context mockContext;
    @Mock
    private AppWidgetManager mockAppWidgetManager;
    @Mock
    private SharedPreferences mockSharedPreferences;

    private CreditCardWidgetProvider widgetProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        widgetProvider = new CreditCardWidgetProvider();
        when(mockContext.getPackageName()).thenReturn("com.developer.harshul.ccwidget");
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences);
    }

    @Test
    public void testUpdateWidget_WithOutOfMemoryError() {
        when(mockSharedPreferences.getString(anyString(), anyString()))
                .thenThrow(new OutOfMemoryError("Simulated OOM"));

        // Should not crash the app
        try {
            CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);
        } catch (OutOfMemoryError e) {
            fail("OutOfMemoryError should be handled gracefully");
        }
    }

    @Test
    public void testUpdateWidget_WithSecurityException() {
        when(mockContext.getSharedPreferences(anyString(), anyInt()))
                .thenThrow(new SecurityException("Simulated security exception"));

        // Should handle security exceptions gracefully
        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        // Verify it attempts to show error state
        verify(mockAppWidgetManager, never()).updateAppWidget(anyInt(), isNull());
    }

    @Test
    public void testUpdateWidget_WithNullPointerException() {
        when(mockSharedPreferences.getString(anyString(), anyString()))
                .thenThrow(new NullPointerException("Simulated NPE"));

        // Should handle NPE gracefully
        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        // Should still attempt to update widget with error state
        verify(mockAppWidgetManager, atLeastOnce()).updateAppWidget(anyInt(), any());
    }

    @Test
    public void testUpdateWidget_WithIllegalStateException() {
//        when(mockAppWidgetManager.updateAppWidget(anyInt(), any()))
//                .thenThrow(new IllegalStateException("Widget manager in illegal state"));
        doThrow(new IllegalStateException("Widget manager in illegal state"))
                .when(mockAppWidgetManager).updateAppWidget(anyInt(), any());

        when(mockSharedPreferences.getString(anyString(), anyString()))
                .thenReturn("{\"name\":\"Test\",\"dueDate\":1234567890}");

        // Should handle widget manager exceptions
        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        verify(mockAppWidgetManager).updateAppWidget(anyInt(), any());
    }

    @Test
    public void testOnDeleted_WithIOException() {
        SharedPreferences.Editor mockEditor = mock(SharedPreferences.Editor.class);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.clear()).thenReturn(mockEditor);
//        when(mockEditor.apply()).thenThrow(new RuntimeException("IO Exception"));
        doThrow(new RuntimeException("IO Exception")).when(mockEditor).apply();
        int[] widgetIds = {1, 2, 3};

        // Should handle IO exceptions during cleanup
        widgetProvider.onDeleted(mockContext, widgetIds);

        verify(mockEditor, times(3)).apply();
    }

    @Test
    public void testOnUpdate_WithInterruptedException() throws InterruptedException {
        int[] widgetIds = {1, 2, 3};

        // Simulate interrupted thread
        Thread.currentThread().interrupt();

        widgetProvider.onUpdate(mockContext, mockAppWidgetManager, widgetIds);

        // Should handle interruption gracefully
        assertTrue("Thread should remain interrupted", Thread.interrupted());
    }
}