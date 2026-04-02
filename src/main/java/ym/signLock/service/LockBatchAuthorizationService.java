package ym.signLock.service;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LockBatchAuthorizationService {

    private final LockService lockService;
    private final LockPlayerNameNormalizer playerNameNormalizer;

    public LockBatchAuthorizationService(LockService lockService, LockPlayerNameNormalizer playerNameNormalizer) {
        this.lockService = lockService;
        this.playerNameNormalizer = playerNameNormalizer;
    }

    public BatchAddSummary addPlayers(Player actor, Sign preferredSign, LockService.LockInfo lock, Collection<String> rawTargets) {
        if (!canManage(lock, actor)) {
            return BatchAddSummary.denied();
        }

        List<String> targets = normalizeTargets(actor, rawTargets);
        List<String> addedPlayers = new ArrayList<>();
        List<String> addedWithExtensionPlayers = new ArrayList<>();
        List<String> alreadyAuthorizedPlayers = new ArrayList<>();
        List<String> noSpacePlayers = new ArrayList<>();

        for (String target : targets) {
            LockService.AddPlayerResult result = lockService.addPlayerToLock(preferredSign, lock, target);
            if (result == LockService.AddPlayerResult.ALREADY_AUTHORIZED) {
                alreadyAuthorizedPlayers.add(target);
            } else if (result == LockService.AddPlayerResult.NO_SPACE) {
                noSpacePlayers.add(target);
            } else {
                addedPlayers.add(target);
                if (result == LockService.AddPlayerResult.ADDED_WITH_EXTENSION) {
                    addedWithExtensionPlayers.add(target);
                }
            }
        }

        return new BatchAddSummary(
                true,
                List.copyOf(addedPlayers),
                List.copyOf(addedWithExtensionPlayers),
                List.copyOf(alreadyAuthorizedPlayers),
                List.copyOf(noSpacePlayers)
        );
    }

    public BatchRemoveSummary removePlayers(Player actor, Sign preferredSign, LockService.LockInfo lock, Collection<String> rawTargets) {
        if (!canManage(lock, actor)) {
            return BatchRemoveSummary.denied();
        }

        List<String> targets = normalizeTargets(actor, rawTargets);
        List<String> removedPlayers = new ArrayList<>();
        List<String> notFoundPlayers = new ArrayList<>();
        List<String> ownerDeniedPlayers = new ArrayList<>();

        for (String target : targets) {
            LockService.RemovePlayerResult result = lockService.removePlayerFromLock(preferredSign, lock, target);
            if (result == LockService.RemovePlayerResult.REMOVED) {
                removedPlayers.add(target);
            } else if (result == LockService.RemovePlayerResult.NOT_FOUND) {
                notFoundPlayers.add(target);
            } else if (result == LockService.RemovePlayerResult.OWNER_DENIED) {
                ownerDeniedPlayers.add(target);
            }
        }

        return new BatchRemoveSummary(
                true,
                List.copyOf(removedPlayers),
                List.copyOf(notFoundPlayers),
                List.copyOf(ownerDeniedPlayers)
        );
    }

    private boolean canManage(LockService.LockInfo lock, Player actor) {
        return lock != null && actor != null && lockService.canManage(lock, actor);
    }

    private List<String> normalizeTargets(Player actor, Collection<String> rawTargets) {
        if (rawTargets == null || rawTargets.isEmpty()) {
            return List.of();
        }

        Map<String, String> orderedUniqueTargets = new LinkedHashMap<>();
        for (String rawTarget : rawTargets) {
            if (rawTarget == null || rawTarget.isBlank()) {
                continue;
            }

            String normalizedTarget = playerNameNormalizer.normalize(actor, rawTarget.trim());
            if (normalizedTarget == null || normalizedTarget.isBlank()) {
                continue;
            }

            orderedUniqueTargets.putIfAbsent(normalizedTarget.toLowerCase(Locale.ROOT), normalizedTarget);
        }
        return new ArrayList<>(orderedUniqueTargets.values());
    }

    public record BatchAddSummary(
            boolean permitted,
            List<String> addedPlayers,
            List<String> addedWithExtensionPlayers,
            List<String> alreadyAuthorizedPlayers,
            List<String> noSpacePlayers
    ) {
        public static BatchAddSummary denied() {
            return new BatchAddSummary(false, List.of(), List.of(), List.of(), List.of());
        }

        public int addedCount() {
            return addedPlayers.size();
        }

        public boolean extensionCreated() {
            return !addedWithExtensionPlayers.isEmpty();
        }
    }

    public record BatchRemoveSummary(
            boolean permitted,
            List<String> removedPlayers,
            List<String> notFoundPlayers,
            List<String> ownerDeniedPlayers
    ) {
        public static BatchRemoveSummary denied() {
            return new BatchRemoveSummary(false, List.of(), List.of(), List.of());
        }
    }
}
