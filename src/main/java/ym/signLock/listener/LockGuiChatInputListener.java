package ym.signLock.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ym.signLock.gui.LockManagementGuiActionService;
import ym.signLock.gui.LockManagementPendingInputStore;

import java.util.function.Consumer;

public final class LockGuiChatInputListener implements Listener {

    private final Consumer<Runnable> nextTick;
    private final LockManagementPendingInputStore pendingInputStore;
    private final LockManagementGuiActionService actionService;

    public LockGuiChatInputListener(
            Consumer<Runnable> nextTick,
            LockManagementPendingInputStore pendingInputStore,
            LockManagementGuiActionService actionService
    ) {
        this.nextTick = nextTick;
        this.pendingInputStore = pendingInputStore;
        this.actionService = actionService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (!pendingInputStore.hasPendingAdd(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();
        nextTick.accept(() -> actionService.handleChatInput(event.getPlayer(), message));
    }
}
