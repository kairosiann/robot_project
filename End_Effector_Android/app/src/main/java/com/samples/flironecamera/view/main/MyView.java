package com.samples.flironecamera.view.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class MyView extends View {
    private Paint paint = new Paint();
    private double x;
    //private double x = 240;
    //private double y = 420;
    private double y;

    public MyView(Context context) {
        this(context, null, 0, 0);
    }

    public MyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("MyView", "onDraw: ");
        canvas.drawCircle((float)x, (float)y+ 100, 100, paint);
        //canvas.drawRect();
    }

    public void setCoord(double mx, double my) {
        this.x = mx;
        this.y = (my / 8) ;
        invalidate();
    }
}
