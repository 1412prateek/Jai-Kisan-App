package com.rcc.jai_kisan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Intelligent, self-healing Gemini API Key Manager.
 * Rotates through keys via round-robin, automatically flags/quarantines failed
 * keys (e.g. 403 Forbidden or 429 Rate Limit) without crashing, and smoothly
 * picks up any new or replaced keys in BuildConfig.
 */
public class GeminiKeyManager {
    private static final List<String> ALL_KEYS = new ArrayList<>();
    private static final Set<String> FAILED_KEYS = Collections.synchronizedSet(new HashSet<>());
    private static final AtomicInteger currentIndex = new AtomicInteger(0);

    static {
        // Load all available keys from BuildConfig
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
            String trimmed = key.trim();
            if (!ALL_KEYS.contains(trimmed)) {
                ALL_KEYS.add(trimmed);
            }
        }
    }

    /**
     * Get the next active key. Skips temporarily or permanently failed keys.
     */
    public static synchronized String getNextKey() {
        if (ALL_KEYS.isEmpty()) {
            return BuildConfig.GEMINI_API_KEY;
        }

        // Try to find a key not marked as failed
        List<String> activeKeys = new ArrayList<>();
        for (String k : ALL_KEYS) {
            if (!FAILED_KEYS.contains(k)) {
                activeKeys.add(k);
            }
        }

        // If all keys were somehow marked failed, reset the failure set to give them another chance
        if (activeKeys.isEmpty()) {
            FAILED_KEYS.clear();
            activeKeys.addAll(ALL_KEYS);
        }

        int index = Math.abs(currentIndex.getAndIncrement() % activeKeys.size());
        return activeKeys.get(index);
    }

    /**
     * Report an error on a key (403, 429, etc.). The manager will quarantine it
     * and automatically try the next active key.
     */
    public static void markKeyFailed(String key) {
        if (key != null && !key.trim().isEmpty()) {
            FAILED_KEYS.add(key.trim());
        }
    }

    /**
     * Total number of configured keys.
     */
    public static int getTotalKeyCount() {
        return ALL_KEYS.size();
    }

    /**
     * Number of currently healthy keys.
     */
    public static int getHealthyKeyCount() {
        return ALL_KEYS.size() - FAILED_KEYS.size();
    }
}
