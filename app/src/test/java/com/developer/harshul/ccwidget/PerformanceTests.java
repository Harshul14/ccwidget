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
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class PerformanceTests {

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

    @Test(timeout = 5000) // 5 second timeout
    public void testWidget_UpdatePerformance() throws Exception {
        JSONArray cardsArray = new JSONArray();
        for (int i = 0; i < 50; i++) {
            JSONObject cardObj = new JSONObject();
            cardObj.put("name", "Performance Card " + i);
            cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(i + 1));
            cardsArray.put(cardObj);
        }

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, i);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Should complete 100 updates within 5 seconds
        assertTrue("Performance test took too long: " + duration + "ms", duration < 5000);
        verify(mockAppWidgetManager, times(100)).updateAppWidget(anyInt(), any());
    }

    @Test(timeout = 10000) // 10 second timeout
    public void testWidget_ConcurrentUpdatesStressTest() throws Exception {
        JSONArray cardsArray = new JSONArray();
        JSONObject cardObj = new JSONObject();
        cardObj.put("name", "Stress Test Card");
        cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        cardsArray.put(cardObj);

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        int threadCount = 10;
        int operationsPerThread = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, threadId);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue("Stress test did not complete in time", latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        verify(mockAppWidgetManager, times(threadCount * operationsPerThread)).updateAppWidget(anyInt(), any());
    }

    @Test
    public void testWidget_MemoryUsageWithLargeData() throws Exception {
        // Create large dataset
        JSONArray cardsArray = new JSONArray();
        for (int i = 0; i < 1000; i++) {
            JSONObject cardObj = new JSONObject();
            // Create large card names
            StringBuilder largeName = new StringBuilder();
            for (int j = 0; j < 100; j++) {
                largeName.append("Card Name Part ").append(j).append(" ");
            }
            cardObj.put("name", largeName.toString());
            cardObj.put("dueDate", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(i + 1));
            cardsArray.put(cardObj);
        }

        when(mockSharedPreferences.getString(anyString(), eq(""))).thenReturn(cardsArray.toString());

        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        CreditCardWidgetProvider.updateAppWidget(mockContext, mockAppWidgetManager, 1);

        System.gc(); // Force garbage collection
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();

        // Memory increase should be reasonable (less than 50MB for this test)
        long memoryIncrease = finalMemory - initialMemory;
        assertTrue("Memory usage too high: " + memoryIncrease + " bytes",
                memoryIncrease < 50 * 1024 * 1024);

        verify(mockAppWidgetManager).updateAppWidget(eq(1), any());
    }
}