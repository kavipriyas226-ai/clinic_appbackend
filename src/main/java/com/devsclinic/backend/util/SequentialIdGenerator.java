package com.devsclinic.backend.util;

import java.util.List;

/**
 * Generates the app's human-readable IDs (PT-1001, INV-3012, MED-101, ...) by finding
 * the highest existing numeric suffix for a given prefix and incrementing it.
 */
public final class SequentialIdGenerator {

    private SequentialIdGenerator() {
    }

    public static String next(List<String> existingIds, String prefix, int fallbackStart) {
        int max = existingIds.stream()
                .filter(id -> id != null && id.startsWith(prefix))
                .map(id -> id.substring(prefix.length()))
                .mapToInt(SequentialIdGenerator::parseOrZero)
                .max()
                .orElse(fallbackStart - 1);
        return prefix + (max + 1);
    }

    private static int parseOrZero(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
