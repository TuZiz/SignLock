package ym.signLock.service;

import org.bukkit.entity.Player;

public final class LockPlayerNameNormalizer {

    private final PlayerIdentityService playerIdentityService;

    public LockPlayerNameNormalizer(PlayerIdentityService playerIdentityService) {
        this.playerIdentityService = playerIdentityService;
    }

    public String normalize(Player actor, String rawInput) {
        playerIdentityService.remember(actor);
        String normalizedTarget = playerIdentityService.resolveStoredName(rawInput);
        playerIdentityService.save();
        return normalizedTarget == null || normalizedTarget.isBlank() ? rawInput : normalizedTarget;
    }
}
