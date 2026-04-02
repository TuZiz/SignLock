package ym.signLock.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LockBatchTargetParser {

    public List<String> parse(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            return List.of();
        }

        Map<String, String> orderedUniqueTargets = new LinkedHashMap<>();
        for (String token : rawInput.split("[,\\s]+")) {
            if (token == null) {
                continue;
            }

            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            orderedUniqueTargets.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed);
        }

        return new ArrayList<>(orderedUniqueTargets.values());
    }
}
