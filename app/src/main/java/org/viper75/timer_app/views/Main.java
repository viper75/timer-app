package org.viper75.timer_app.views;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.viper75.timer_app.databinding.MainLayoutBinding;
import org.viper75.timer_app.services.TimerService;
import org.viper75.timer_app.utils.ProgressBarAnimation;

import static org.viper75.timer_app.services.TimerService.TIMER_INTENT_ACTION;

public class Main extends AppCompatActivity {

    public static final String TIMER_VALUE = "TimerValue";
    private final int PROGRESS_BAR_MAX = 100;

    private MainLayoutBinding mMainLayoutBinding;
    private ProgressBar mTimerProgressBar;
    private BroadcastReceiver mTimerBroadcastReceiver;
    private Intent timerServiceIntent;

    private TimerService mTimerService;
    private boolean mTimerServiceBound = false;

    private ServiceConnection mTimerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTimerService = ((TimerService.TimerServiceBinder) service).getService();
            mTimerServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTimerService = null;
            mTimerServiceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainLayoutBinding = MainLayoutBinding.inflate(getLayoutInflater());
        setContentView(mMainLayoutBinding.getRoot());

        mTimerBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int timeRemaining = intent.getIntExtra(TIMER_VALUE, 0);
                setTimerText(timeRemaining);
            }
        };

        timerServiceIntent = new Intent(this, TimerService.class);
        timerServiceIntent.putExtra(TIMER_VALUE, 60);
        startService(timerServiceIntent);

        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(timerServiceIntent, mTimerServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mTimerBroadcastReceiver, new IntentFilter(TIMER_INTENT_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTimerServiceBound) {
            unbindService(mTimerServiceConnection);
        }
        unregisterReceiver(mTimerBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(timerServiceIntent);
    }

    private void initViews() {
        FloatingActionButton startTimerBtn = mMainLayoutBinding.startTimer;
        startTimerBtn.setOnClickListener(v -> startTimer());

        mMainLayoutBinding.stopTimer.setOnClickListener(v -> stopTimer());

        mTimerProgressBar = mMainLayoutBinding.timerProgressBar;
        mTimerProgressBar.setMax(PROGRESS_BAR_MAX);
    }

    private void startTimer() {
        mTimerService.startTimer();
    }

    private void stopTimer() {
        mTimerService.stopTimer();
    }

    private void setTimerText(int timer) {
        int progress = (int) ((timer / 60.0) * PROGRESS_BAR_MAX);
        int from = mTimerProgressBar.getProgress();

        ProgressBarAnimation anim = new ProgressBarAnimation(mTimerProgressBar, from, progress);
        anim.setDuration(50);
        mTimerProgressBar.startAnimation(anim);

        int hours = timer / 3600;
        int minutes = (timer % 3600) / 60;
        int seconds = timer % 60;

        @SuppressLint("DefaultLocale")
        String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        mMainLayoutBinding.timerText.setText(time);
    }
}