package com.developer.harshul.ccwidget;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationReminderService extends BroadcastReceiver {

    private static final String CHANNEL_ID = "credit_card_reminders";
    private static final String PREFS_NAME = "CCWidgetPrefs";
    private static final String CARDS_DATA_KEY = "cards_data";
    private static final String ACTION_REMINDER = "ACTION_CREDIT_CARD_REMINDER";
    private static final String ACTION_DUE_DATE_ALARM = "ACTION_DUE_DATE_ALARM";
    private static final String EXTRA_CARD_NAME = "card_name";
    private static final String EXTRA_DAYS_REMAINING = "days_remaining";
    private static final String EXTRA_WIDGET_ID = "widget_id";

    private static MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_REMINDER.equals(action)) {
            handleReminder(context, intent);
        } else if (ACTION_DUE_DATE_ALARM.equals(action)) {
            handleDueDateAlarm(context, intent);
        }
    }

    private void handleReminder(Context context, Intent intent) {
        String cardName = intent.getStringExtra(EXTRA_CARD_NAME);
        int daysRemaining = intent.getIntExtra(EXTRA_DAYS_REMAINING, 0);

        // Create notification
        createNotificationChannel(context);

        String title;
        String message;

        if (daysRemaining == 0) {
            title = "Credit Card Payment Due TODAY!";
            message = cardName + " payment is due today";
        } else if (daysRemaining == 1) {
            title = "Credit Card Payment Due Tomorrow";
            message = cardName + " payment is due in 1 day";
        } else {
            title = "Credit Card Payment Reminder";
            message = cardName + " payment is due in " + daysRemaining + " days";
        }

        // Vibration pattern
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (daysRemaining == 0) {
                    // Stronger vibration for due date
                    vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 500, 200, 500, 200, 500}, -1));
                } else {
                    // Gentle vibration for reminders
                    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            } else {
                if (daysRemaining == 0) {
                    vibrator.vibrate(new long[]{0, 500, 200, 500, 200, 500}, -1);
                } else {
                    vibrator.vibrate(300);
                }
            }
        }

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(daysRemaining == 0 ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (daysRemaining == 0) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(cardName.hashCode(), builder.build());
    }

    private void handleDueDateAlarm(Context context, Intent intent) {
        String cardName = intent.getStringExtra(EXTRA_CARD_NAME);

        // Check if current time is between 10 AM and 5 PM
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);

        if (currentHour >= 10 && currentHour < 17) {
            // Play alarm sound
            playAlarmSound(context);

            // Strong vibration
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Continuous vibration pattern
                    vibrator.vibrate(VibrationEffect.createWaveform(
                            new long[]{0, 1000, 500, 1000, 500, 1000},
                            new int[]{0, 255, 0, 255, 0, 255},
                            0 // Repeat from index 0
                    ));
                } else {
                    vibrator.vibrate(new long[]{0, 1000, 500, 1000, 500, 1000}, 0);
                }
            }

            // Show urgent notification
            createNotificationChannel(context);

            Intent stopAlarmIntent = new Intent(context, AlarmStopReceiver.class);
            PendingIntent stopAlarmPendingIntent = PendingIntent.getBroadcast(
                    context, 0, stopAlarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("URGENT: Credit Card Payment Due!")
                    .setContentText(cardName + " payment is due TODAY! Don't miss it!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setFullScreenIntent(stopAlarmPendingIntent, true)
                    .addAction(R.drawable.ic_launcher_foreground, "Stop Alarm", stopAlarmPendingIntent)
                    .setOngoing(true)
                    .setAutoCancel(false);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(9999, builder.build());

            // Schedule to stop alarm after 2 minutes if not manually stopped
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent stopIntent = new Intent(context, AlarmStopReceiver.class);
            PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                    context, 9999, stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                long stopTime = System.currentTimeMillis() + (2 * 60 * 1000); // 2 minutes
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, stopTime, stopPendingIntent);
            }
        } else {
            // Schedule next check at 10 AM if before, or next day at 10 AM if after 5 PM
            scheduleNextDueDateAlarmCheck(context, cardName);
        }
    }

    private void playAlarmSound(Context context) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, alarmUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void scheduleNextDueDateAlarmCheck(Context context, String cardName) {
        Calendar nextCheck = Calendar.getInstance();
        int currentHour = nextCheck.get(Calendar.HOUR_OF_DAY);

        if (currentHour < 10) {
            // Before 10 AM, schedule for today at 10 AM
            nextCheck.set(Calendar.HOUR_OF_DAY, 10);
            nextCheck.set(Calendar.MINUTE, 0);
            nextCheck.set(Calendar.SECOND, 0);
        } else {
            // After 5 PM, schedule for next day at 10 AM
            nextCheck.add(Calendar.DAY_OF_MONTH, 1);
            nextCheck.set(Calendar.HOUR_OF_DAY, 10);
            nextCheck.set(Calendar.MINUTE, 0);
            nextCheck.set(Calendar.SECOND, 0);
        }

        Intent intent = new Intent(context, NotificationReminderService.class);
        intent.setAction(ACTION_DUE_DATE_ALARM);
        intent.putExtra(EXTRA_CARD_NAME, cardName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, cardName.hashCode() + 1000, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextCheck.getTimeInMillis(), pendingIntent);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Credit Card Reminders";
            String description = "Notifications for credit card payment reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 300, 200, 300});

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void scheduleReminders(Context context, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME + widgetId, Context.MODE_PRIVATE);
        String cardsDataJson = prefs.getString(CARDS_DATA_KEY, "");

        if (cardsDataJson.isEmpty()) return;

        try {
            JSONArray cardsArray = new JSONArray(cardsDataJson);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            for (int i = 0; i < cardsArray.length(); i++) {
                JSONObject cardObj = cardsArray.getJSONObject(i);
                String cardName = cardObj.getString("name");
                long dueDate = cardObj.getLong("dueDate");

                // Cancel existing alarms for this card
                cancelReminders(context, cardName);

                // Schedule reminders for 3 days, 1 day, and due date
                scheduleReminder(context, alarmManager, cardName, dueDate, 3);
                scheduleReminder(context, alarmManager, cardName, dueDate, 1);
                scheduleReminder(context, alarmManager, cardName, dueDate, 0);

                // Schedule due date alarm (between 10 AM - 5 PM)
                scheduleDueDateAlarm(context, alarmManager, cardName, dueDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scheduleReminder(Context context, AlarmManager alarmManager, String cardName, long dueDate, int daysBefore) {
        Calendar reminderTime = Calendar.getInstance();
        reminderTime.setTimeInMillis(dueDate);
        reminderTime.add(Calendar.DAY_OF_MONTH, -daysBefore);
        reminderTime.set(Calendar.HOUR_OF_DAY, 9); // 9 AM reminder
        reminderTime.set(Calendar.MINUTE, 0);
        reminderTime.set(Calendar.SECOND, 0);

        // Only schedule if reminder time is in the future
        if (reminderTime.getTimeInMillis() > System.currentTimeMillis()) {
            Intent intent = new Intent(context, NotificationReminderService.class);
            intent.setAction(ACTION_REMINDER);
            intent.putExtra(EXTRA_CARD_NAME, cardName);
            intent.putExtra(EXTRA_DAYS_REMAINING, daysBefore);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, cardName.hashCode() + daysBefore, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private static void scheduleDueDateAlarm(Context context, AlarmManager alarmManager, String cardName, long dueDate) {
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(dueDate);
        alarmTime.set(Calendar.HOUR_OF_DAY, 10); // Start at 10 AM
        alarmTime.set(Calendar.MINUTE, 0);
        alarmTime.set(Calendar.SECOND, 0);

        // Only schedule if due date is today or in the future
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        if (alarmTime.getTimeInMillis() >= today.getTimeInMillis()) {
            Intent intent = new Intent(context, NotificationReminderService.class);
            intent.setAction(ACTION_DUE_DATE_ALARM);
            intent.putExtra(EXTRA_CARD_NAME, cardName);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, cardName.hashCode() + 1000, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
            }
        }
    }

    public static void cancelReminders(Context context, String cardName) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Cancel reminder alarms
        for (int days : new int[]{0, 1, 3}) {
            Intent intent = new Intent(context, NotificationReminderService.class);
            intent.setAction(ACTION_REMINDER);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, cardName.hashCode() + days, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }

        // Cancel due date alarm
        Intent dueDateIntent = new Intent(context, NotificationReminderService.class);
        dueDateIntent.setAction(ACTION_DUE_DATE_ALARM);
        PendingIntent dueDatePendingIntent = PendingIntent.getBroadcast(
                context, cardName.hashCode() + 1000, dueDateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(dueDatePendingIntent);
        }
    }
}