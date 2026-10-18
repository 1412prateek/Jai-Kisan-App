package com.rcc.jai_kisan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CircularHealthScoreView extends View {

    private int score = 50;
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF rectF;
    private float strokeWidth = 18f;

    public CircularHealthScoreView(Context context) {
        super(context);
        init();
    }

    public CircularHealthScoreView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularHealthScoreView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        strokeWidth = getResources().getDisplayMetrics().density * 6.5f;

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        backgroundPaint.setColor(0xFFECEFF1);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(getResources().getDisplayMetrics().density * 24f);
        textPaint.setFakeBoldText(true);

        rectF = new RectF();

        updateColors();
    }

    private void updateColors() {
        int color;
        if (score >= 70) {
            color = 0xFF2E7D32; // Green
        } else if (score >= 40) {
            color = 0xFFF59E0B; // Amber
        } else {
            color = 0xFFD32F2F; // Red
        }
        if (progressPaint != null) progressPaint.setColor(color);
        if (textPaint != null) textPaint.setColor(color);
    }

    public void setScore(int score) {
        this.score = Math.max(0, Math.min(100, score));
        updateColors();
        invalidate();
    }

    public int getScore() {
        return score;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultSize = (int) (getResources().getDisplayMetrics().density * 82f);
        int width = resolveSize(defaultSize, widthMeasureSpec);
        int height = resolveSize(defaultSize, heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float padding = strokeWidth / 2f + 4f;
        rectF.set(padding, padding, getWidth() - padding, getHeight() - padding);

        // Draw background track arc (open at bottom: 270 degrees total, start at 135 degrees)
        canvas.drawArc(rectF, 135f, 270f, false, backgroundPaint);

        // Draw progress arc
        float sweepAngle = (score / 100f) * 270f;
        if (sweepAngle > 0.5f) {
            canvas.drawArc(rectF, 135f, sweepAngle, false, progressPaint);
        }

        // Draw center score number (centered horizontally and vertically)
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        float textOffsetY = (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(String.valueOf(score), centerX, centerY - textOffsetY, textPaint);
    }
}
