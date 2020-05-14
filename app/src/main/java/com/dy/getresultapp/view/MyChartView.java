package com.dy.getresultapp.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class MyChartView extends View {
    public MyChartView(Context context) {
        super(context);
    }
    public MyChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }



}
