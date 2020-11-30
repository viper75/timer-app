package org.viper75.timer_app.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.viper75.timer_app.R;
import org.viper75.timer_app.databinding.MainLayoutBinding;
import org.viper75.timer_app.services.TimerService;
import org.viper75.timer_app.utils.ProgressBarAnimation;

import static org.viper75.timer_app.services.TimerService.TIMER_INTENT_ACTION;

public class Main extends AppCompatActivity {

    public static final String TIMER_VALUE = "TimerValue";
    private final int PROGRESS_BAR_MAX = 100;

    private MainLayoutBinding mMainLayoutBinding;
    private ProgressBar mTimerProgressBar;
    private FloatingActionButton mStartTimerBtn;
    private BroadcastReceiver mTimerBroadcastReceiver;
    private Intent timerServiceIntent;

    private TimerService mTimerService;
    private boolean mTimerServiceBound = false;

    private boolean isRunning = false;

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

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey(TIMER_VALUE)) {
                Log.i(Main.class.getSimpleName(), "" + extras.getInt(TIMER_VALUE));
            }
        }

        timerServiceIntent = new Intent(this, TimerService.class);
        timerServiceIntent.putExtra(TIMER_VALUE, 60);

        if (!isServiceRunning(TimerService.class)) {
            startService(timerServiceIntent);
        }

        mMainLayoutBinding = MainLayoutBinding.inflate(getLayoutInflater());
        setContentView(mMainLayoutBinding.getRoot());

        mTimerBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int timeRemaining = intent.getIntExtra(TIMER_VALUE, 0);
                setTimerText(timeRemaining);
            }
        };

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
        mStartTimerBtn = mMainLayoutBinding.startTimer;
        mStartTimerBtn.setOnClickListener(v -> startTimer());

        mMainLayoutBinding.stopTimer.setOnClickListener(v -> stopTimer());

        mTimerProgressBar = mMainLayoutBinding.timerProgressBar;
        mTimerProgressBar.setMax(PROGRESS_BAR_MAX);
    }

    private void startTimer() {
        isRunning = !isRunning;

        if (isRunning) {
            mStartTimerBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause));
        } else {
            mStartTimerBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow));
        }

        mTimerService.startTimer();
    }

    private void stopTimer() {
        if (isRunning) {
            isRunning = false;
            mStartTimerBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow));
        }
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

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager activityManager = getSystemService(ActivityManager.class);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}