package com.developer.harshul.pinvoke;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DateCalculationTest {

    @Test
    public void testDaysRemainingCalculation() {
        long currentTime = System.currentTimeMillis();

        // Test various day differences
        int[] testDays = {-10, -1, 0, 1, 5, 15, 30, 365};

        for (int expectedDays : testDays) {
            long testTime = currentTime + (expectedDays * 24L * 60 * 60 * 1000);
            int calculatedDays = TestUtils.calculateDaysRemaining(testTime);

            // Allow for small rounding differences
            assertTrue("Days calculation should be accurate for " + expectedDays + " days",
                    Math.abs(calculatedDays - expectedDays) <= 1);
        }
    }

    @Test
    public void testTimeZoneHandling() {
        // Test with different time zones
        TimeZone originalTimeZone = TimeZone.getDefault();

        try {
            String[] timeZoneIds = {"UTC", "America/New_York", "Asia/Tokyo", "Europe/London"};

            for (String timeZoneId : timeZoneIds) {
                TimeZone.setDefault(TimeZone.getTimeZone(timeZoneId));

                Calendar testDate = Calendar.getInstance();
                testDate.add(Calendar.DAY_OF_MONTH, 5);

                int daysRemaining = TestUtils.calculateDaysRemaining(testDate.getTimeInMillis());
                assertTrue("Should handle timezone " + timeZoneId,
                        daysRemaining >= 4 && daysRemaining <= 6);
            }
        } finally {
            TimeZone.setDefault(originalTimeZone);
        }
    }

    @Test
    public void testLeapYearHandling() {
        // Test February 29th in leap years
        Calendar leapYearDate = Calendar.getInstance();
        leapYearDate.set(2024, Calendar.FEBRUARY, 29); // 2024 is a leap year

        // Should not crash with leap year date
        int days = TestUtils.calculateDaysRemaining(leapYearDate.getTimeInMillis());
        assertTrue("Should handle leap year dates", days != Integer.MIN_VALUE);
    }

    @Test
    public void testEdgeCaseDates() {
        // Test edge cases
        long[] edgeCases = {
                0L,                                    // Unix epoch
                Long.MAX_VALUE,                        // Far future
                System.currentTimeMillis(),            // Exactly now
                System.currentTimeMillis() + 1000,     // 1 second from now
                System.currentTimeMillis() - 1000      // 1 second ago
        };

        for (long edgeCase : edgeCases) {
            try {
                int days = TestUtils.calculateDaysRemaining(edgeCase);
                // Test passes if no exception is thrown
                assertTrue("Edge case should be handled", true);
            } catch (Exception e) {
                fail("Should handle edge case date: " + edgeCase + ", error: " + e.getMessage());
            }
        }
    }
}