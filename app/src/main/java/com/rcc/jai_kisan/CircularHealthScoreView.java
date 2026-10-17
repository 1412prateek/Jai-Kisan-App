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
    private Paint subTextPaint;
    private RectF rectF;
    private float strokeWidth = 24f;

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
        strokeWidth = getResources().getDisplayMetrics().density * 8f;

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setColor(0xFFE0E0E0);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(0xFF2E7D32);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(getResources().getDisplayMetrics().density * 26f);
        textPaint.setFakeBoldText(true);
        textPaint.setColor(0xFF212121);

        subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subTextPaint.setTextAlign(Paint.Align.CENTER);
        subTextPaint.setTextSize(getResources().getDisplayMetrics().density * 11f);
        subTextPaint.setColor(0xFF757575);

        rectF = new RectF();
    }

    public void setScore(int score) {
        this.score = Math.max(0, Math.min(100, score));

        if (this.score >= 70) {
            progressPaint.setColor(0xFF2E7D32);
        } else if (this.score >= 40) {
            progressPaint.setColor(0xFFF59E0B);
        } else {
            progressPaint.setColor(0xFFD32F2F);
        }
        invalidate();
    }

    public int getScore() {
        return score;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultSize = (int) (getResources().getDisplayMetrics().density * 110f);
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

        // Draw track
        canvas.drawArc(rectF, -90f, 360f, false, backgroundPaint);

        // Draw progress
        float sweepAngle = (score / 100f) * 360f;
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint);

        // Draw center text
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        float textOffsetY = (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(String.valueOf(score), centerX, centerY - textOffsetY - 4f, textPaint);
        canvas.drawText("/ 100", centerX, centerY + textPaint.getTextSize() / 2f + 4f, subTextPaint);
    }
}
