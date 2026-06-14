package com.mobile.madfya.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class GaugeView extends View {
    private int percent = 96;
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    public GaugeView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        trackPaint.setStyle(Paint.Style.STROKE); trackPaint.setStrokeWidth(10f); trackPaint.setColor(0xFFE0E0E0);
        fillPaint.setStyle(Paint.Style.STROKE);  fillPaint.setStrokeWidth(10f);  fillPaint.setColor(0xFF4CAF50);
        fillPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setPercent(int p) { percent = p; invalidate(); }

    @Override protected void onDraw(Canvas canvas) {
        float cx = getWidth() / 2f, cy = getHeight() / 2f, r = Math.min(cx, cy) - 12f;
        RectF oval = new RectF(cx - r, cy - r, cx + r, cy + r);
        canvas.drawArc(oval, -90, 360, false, trackPaint);
        canvas.drawArc(oval, -90, 3.6f * percent, false, fillPaint);
    }
}