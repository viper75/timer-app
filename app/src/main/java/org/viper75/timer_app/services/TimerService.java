package org.viper75.timer_app.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.viper75.timer_app.MyTimer;
import org.viper75.timer_app.R;
import org.viper75.timer_app.views.Main;

import java.util.concurrent.atomic.AtomicInteger;

import static org.viper75.timer_app.views.Main.TIMER_VALUE;

public class TimerService extends Service {

    public static final String TIMER_INTENT_ACTION = "ACTION_TIMER";
    private final int NOTIFICATION_REQUEST_CODE = 1000;
    private final int NOTIFICATION_ID = 2000;

    private IBinder binder = new TimerServiceBinder();
    private boolean isRunning;
    private int timeRemaining;

    public class TimerServiceBinder extends Binder {
        public TimerService getService() { return  TimerService.this; }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timeRemaining = intent.getIntExtra(TIMER_VALUE, 0);

        final Handler handler = MyTimer.MAIN_HANDLER;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent timerIntent = new Intent();
                timerIntent.setAction(TIMER_INTENT_ACTION);

                if (isRunning) {
                    timeRemaining--;

                    showNotificationUpdate();

                    if (timeRemaining >= 0) {
                        timerIntent.putExtra(TIMER_VALUE, timeRemaining);
                        sendBroadcast(timerIntent);
                    }
                }

                handler.postDelayed( this, 1000);
            }
        }, 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void startTimer() {
        isRunning = true;
    }

    public void stopTimer() {
        isRunning = false;
    }


    private void showNotificationUpdate() {
        Intent notificationIntent = new Intent(this, Main.class);
        notificationIntent.putExtra(TIMER_VALUE, timeRemaining);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_REQUEST_CODE,
                notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentText(getTimerValue())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        startForeground(NOTIFICATION_ID, builder.build());
    }

    @SuppressLint("DefaultLocale")
    private String getTimerValue() {
        int hours = timeRemaining / 3600;
        int minutes = (timeRemaining % 3600) / 60;
        int seconds = timeRemaining % 60;

        if (hours == 0)
            return String.format("%02d:%02d", minutes, seconds);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
