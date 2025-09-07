package com.developer.harshul.pinvoke;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.junit.Test;
import org.mockito.Mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ConcurrencyTests extends BaseTest {

    @Mock
    private AppWidgetManager mockAppWidgetManager;

    @Mock
    private SharedPreferences mockPrefs;

    @Test
    public void testConcurrentWidgetUpdates() throws InterruptedException {
        // Given - Multiple widgets updating concurrently
        int numberOfThreads = 10;
        int[] widgetIds = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        when(mockPrefs.getString(anyString(), anyString())).thenReturn("");
        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
                .thenReturn(mockPrefs);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // When - Update widgets concurrently
        for (int widgetId : widgetIds) {
            executor.submit(() -> {
                try {
                    CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, widgetId);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Then - All updates should complete without issues
        assertTrue("All concurrent updates should complete",
                latch.await(5, TimeUnit.SECONDS));

        verify(mockAppWidgetManager, times(widgetIds.length))
                .updateAppWidget(anyInt(), any());

        executor.shutdown();
    }

    @Test
    public void testConcurrentDataAccess() throws Exception {
        // Given - Multiple threads accessing shared preferences
        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // Mock thread-safe SharedPreferences behavior
        when(mockPrefs.getString(anyString(), anyString())).thenReturn("[]");
        when(mockPrefs.edit()).thenReturn(mock(SharedPreferences.Editor.class));

        Context spyContext = spy(context);
        when(spyContext.getSharedPreferences(anyString(), eq(Context.MODE_PRIVATE)))
                .thenReturn(mockPrefs);

        // When - Multiple threads access data simultaneously
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    // Simulate data access and widget update
                    CreditCardWidgetProvider.updateAppWidget(spyContext, mockAppWidgetManager, threadId + 1);
                } catch (Exception e) {
                    fail("Concurrent access should not cause exceptions: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Then - All operations should complete successfully
        assertTrue("All concurrent operations should complete",
                latch.await(3, TimeUnit.SECONDS));

        executor.shutdown();
    }
}