package ym.signLock.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ym.signLock.service.PlayerIdentityService;

public final class PlayerIdentityListener implements Listener {

    private final PlayerIdentityService playerIdentityService;

    public PlayerIdentityListener(PlayerIdentityService playerIdentityService) {
        this.playerIdentityService = playerIdentityService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerIdentityService.remember(event.getPlayer());
        playerIdentityService.saveIfDirty();
    }
}
