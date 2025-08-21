package com.developer.harshul.ccwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

public class MockFactory {

    public static TestMocks createMocks() {
        TestMocks mocks = new TestMocks();
        MockitoAnnotations.openMocks(mocks);

        // Setup common mock behaviors
        when(mocks.mockContext.getPackageName()).thenReturn("com.developer.harshul.ccwidget");
        when(mocks.mockContext.getSharedPreferences(anyString(), anyInt()))
                .thenReturn(mocks.mockSharedPreferences);

        SharedPreferences.Editor mockEditor = mock(SharedPreferences.Editor.class);
        when(mocks.mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.commit()).thenReturn(true);
//        when(mockEditor.apply()).thenReturn(mockEditor);
        doNothing().when(mockEditor).apply();
        when(mockEditor.clear()).thenReturn(mockEditor);

        return mocks;
    }

    public static class TestMocks {
        @Mock
        public Context mockContext;

        @Mock
        public AppWidgetManager mockAppWidgetManager;

        @Mock
        public SharedPreferences mockSharedPreferences;
    }
}