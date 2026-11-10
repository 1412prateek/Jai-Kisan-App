package com.rcc.jai_kisan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Gemini Live Nebula Visualizer
 * Renders smooth animated glowing circular ripples and pulsating concentric rings
 * inspired by Google Gemini Live.
 */
public class GeminiLiveVisualizerView extends View {

    private Paint ringPaint1;
    private Paint ringPaint2;
    private Paint ringPaint3;
    private Paint centerGlowPaint;

    private float animPhase = 0f;
    private float audioAmplitude = 0.2f;
    private boolean isListening = false;
    private boolean isSpeaking = false;
    private boolean isThinking = false;

    private final RectF ovalBounds = new RectF();

    public GeminiLiveVisualizerView(Context context) {
        super(context);
        init();
    }

    public GeminiLiveVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GeminiLiveVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ringPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint1.setStyle(Paint.Style.STROKE);
        ringPaint1.setStrokeWidth(3f);

        ringPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint2.setStyle(Paint.Style.STROKE);
        ringPaint2.setStrokeWidth(2f);

        ringPaint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint3.setStyle(Paint.Style.STROKE);
        ringPaint3.setStrokeWidth(1.5f);

        centerGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerGlowPaint.setStyle(Paint.Style.FILL);
    }

    public void setListeningState(boolean listening) {
        this.isListening = listening;
        this.isSpeaking = false;
        this.isThinking = false;
        postInvalidateOnAnimation();
    }

    public void setSpeakingState(boolean speaking) {
        this.isSpeaking = speaking;
        this.isListening = false;
        this.isThinking = false;
        postInvalidateOnAnimation();
    }

    public void setThinkingState(boolean thinking) {
        this.isThinking = thinking;
        this.isListening = false;
        this.isSpeaking = false;
        postInvalidateOnAnimation();
    }

    public void setIdleState() {
        this.isListening = false;
        this.isSpeaking = false;
        this.isThinking = false;
        postInvalidateOnAnimation();
    }

    public void setAudioAmplitude(float amp) {
        this.audioAmplitude = Math.max(0.1f, Math.min(amp, 1.0f));
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float baseRadius = Math.min(cx, cy) * 0.42f;

        animPhase += 0.05f;
        if (animPhase > (float) (Math.PI * 2)) {
            animPhase = 0f;
        }

        // Color theme adapts:
        // Listening: Bright Cyan / Electric Blue (Gemini signature)
        // Speaking: Emerald Green / Teal
        // Thinking: Purple / Pink Nebula
        // Idle: Soft White / Ice Blue
        int primaryColor;
        int secondaryColor;

        if (isListening) {
            primaryColor = Color.argb(190, 78, 161, 255);    // Gemini Electric Blue
            secondaryColor = Color.argb(120, 0, 229, 255);  // Bright Aqua
        } else if (isSpeaking) {
            primaryColor = Color.argb(200, 76, 217, 100);   // Emerald Live Green
            secondaryColor = Color.argb(130, 168, 240, 133);
        } else if (isThinking) {
            primaryColor = Color.argb(210, 187, 134, 252);  // Nebula Violet
            secondaryColor = Color.argb(140, 244, 143, 177); // Soft Rose Pink
        } else {
            primaryColor = Color.argb(100, 255, 255, 255);  // Subtle Glass
            secondaryColor = Color.argb(60, 144, 202, 249);
        }

        float pulse = (float) Math.sin(animPhase);
        float ampFactor = isSpeaking || isListening ? (1.0f + audioAmplitude * 0.8f) : (1.0f + pulse * 0.06f);

        // Center ambient aura
        centerGlowPaint.setColor(secondaryColor);
        centerGlowPaint.setAlpha((int) (40 + 30 * Math.abs(pulse)));
        canvas.drawCircle(cx, cy, baseRadius * 1.1f * ampFactor, centerGlowPaint);

        // Ring 1 (Inner)
        ringPaint1.setColor(primaryColor);
        float r1 = baseRadius * 0.95f * ampFactor;
        canvas.drawCircle(cx, cy, r1, ringPaint1);

        // Ring 2 (Middle breathing)
        ringPaint2.setColor(secondaryColor);
        float r2 = baseRadius * (1.15f + 0.12f * (float) Math.cos(animPhase)) * ampFactor;
        canvas.drawCircle(cx, cy, r2, ringPaint2);

        // Ring 3 (Outer fading halo)
        ringPaint3.setColor(primaryColor);
        ringPaint3.setAlpha((int) (60 + 50 * pulse));
        float r3 = baseRadius * (1.35f + 0.18f * (float) Math.sin(animPhase * 0.7f)) * ampFactor;
        canvas.drawCircle(cx, cy, r3, ringPaint3);

        // Keep animating smoothly if not purely idle or if pulsing
        postInvalidateOnAnimation();
    }
}

