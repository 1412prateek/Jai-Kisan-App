package com.rcc.jai_kisan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class PriceTrendChartView extends View {

    private int[] priceData = null;
    private Paint linePaint;
    private Paint fillPaint;
    private Paint dotPaint;
    private Paint dotInnerPaint;
    private Paint textPaint;
    private Paint dayPaint;
    private Path linePath;
    private Path fillPath;

    public PriceTrendChartView(Context context) {
        super(context);
        init();
    }

    public PriceTrendChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PriceTrendChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(density * 2.5f);
        linePaint.setColor(0xFF294FF6);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(0xFF294FF6);

        dotInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotInnerPaint.setStyle(Paint.Style.FILL);
        dotInnerPaint.setColor(Color.WHITE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(density * 10f);
        textPaint.setFakeBoldText(true);
        textPaint.setColor(0xFF212121);

        dayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayPaint.setTextAlign(Paint.Align.CENTER);
        dayPaint.setTextSize(density * 10f);
        dayPaint.setColor(0xFF757575);

        linePath = new Path();
        fillPath = new Path();
    }

    public void setPriceData(int[] prices) {
        this.priceData = prices;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultHeight = (int) (getResources().getDisplayMetrics().density * 140f);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = resolveSize(defaultHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (priceData == null || priceData.length == 0) return;

        int n = priceData.length;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int p : priceData) {
            if (p < min) min = p;
            if (p > max) max = p;
        }

        if (min == max) {
            min -= 10;
            max += 10;
        }

        float density = getResources().getDisplayMetrics().density;
        float paddingLeft = density * 24f;
        float paddingRight = density * 24f;
        float paddingTop = density * 26f;
        float paddingBottom = density * 24f;

        float width = getWidth() - paddingLeft - paddingRight;
        float height = getHeight() - paddingTop - paddingBottom;

        float stepX = n > 1 ? width / (n - 1) : width;

        linePath.reset();
        fillPath.reset();

        float[] pointsX = new float[n];
        float[] pointsY = new float[n];

        for (int i = 0; i < n; i++) {
            float x = paddingLeft + i * stepX;
            float norm = (float) (priceData[i] - min) / (max - min);
            float y = paddingTop + height - (norm * height);

            pointsX[i] = x;
            pointsY[i] = y;

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, paddingTop + height);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }

        fillPath.lineTo(pointsX[n - 1], paddingTop + height);
        fillPath.close();

        // Gradient for fill
        fillPaint.setShader(new LinearGradient(0, paddingTop, 0, paddingTop + height,
                0x33294FF6, 0x05294FF6, Shader.TileMode.CLAMP));

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        // Draw points, prices and day labels
        for (int i = 0; i < n; i++) {
            float x = pointsX[i];
            float y = pointsY[i];

            canvas.drawCircle(x, y, density * 4.5f, dotPaint);
            canvas.drawCircle(x, y, density * 2.5f, dotInnerPaint);

            // Price above point
            canvas.drawText("₹" + priceData[i], x, y - density * 7f, textPaint);

            // Day label below axis
            canvas.drawText("D" + (i + 1), x, getHeight() - density * 6f, dayPaint);
        }
    }
}
