package org.viper75.timer_app.utils;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProgressBarAnimation extends Animation {
    private ProgressBar progressBar;
    private float from;
    private float to;

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float value = from + (to - from) * interpolatedTime;
        Log.i("TAG", value + "");
        progressBar.setProgress((int) value);
    }
}
