package com.rcc.jai_kisan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GeminiKeyManager {
    private static final List<String> KEYS = new ArrayList<>();
    private static final AtomicInteger currentIndex = new AtomicInteger(0);

    static {
        addKeyIfValid(BuildConfig.GEMINI_API_KEY);
        addKeyIfValid(BuildConfig.GEMINI_KEY_1);
        addKeyIfValid(BuildConfig.GEMINI_KEY_2);
        addKeyIfValid(BuildConfig.GEMINI_KEY_3);
        addKeyIfValid(BuildConfig.GEMINI_KEY_4);
        addKeyIfValid(BuildConfig.GEMINI_KEY_5);
        addKeyIfValid(BuildConfig.GEMINI_KEY_6);
        addKeyIfValid(BuildConfig.GEMINI_KEY_7);
        addKeyIfValid(BuildConfig.GEMINI_KEY_8);
        addKeyIfValid(BuildConfig.GEMINI_KEY_9);
        addKeyIfValid(BuildConfig.GEMINI_KEY_10);
    }

    private static void addKeyIfValid(String key) {
        if (key != null && !key.trim().isEmpty() && !key.equals("null") && !key.equals("\"null\"")) {
            KEYS.add(key.trim());
        }
    }

    public static synchronized String getNextKey() {
        if (KEYS.isEmpty()) {
            return BuildConfig.GEMINI_API_KEY;
        }
        int index = Math.abs(currentIndex.getAndIncrement() % KEYS.size());
        return KEYS.get(index);
    }
}
