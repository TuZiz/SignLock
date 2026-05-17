package ym.signLock.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import ym.signLock.gui.LockManagementGuiActionService;
import ym.signLock.gui.LockManagementPendingInputStore;
import ym.signLock.platform.SignLockScheduler;

import java.util.function.Consumer;

public final class LockGuiChatInputListener implements Listener {

    private final SignLockScheduler scheduler;
    private final LockManagementPendingInputStore pendingInputStore;
    private final LockManagementGuiActionService actionService;

    public LockGuiChatInputListener(
            Consumer<Runnable> nextTick,
            LockManagementPendingInputStore pendingInputStore,
            LockManagementGuiActionService actionService
    ) {
        this(schedulerFromConsumer(nextTick), pendingInputStore, actionService);
    }

    public LockGuiChatInputListener(
            SignLockScheduler scheduler,
            LockManagementPendingInputStore pendingInputStore,
            LockManagementGuiActionService actionService
    ) {
        this.scheduler = scheduler;
        this.pendingInputStore = pendingInputStore;
        this.actionService = actionService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        if (capturePendingInput(event.getPlayer(), message)) {
            suppressLegacyChat(event);
        }
    }

    public boolean hasPendingAdd(Player player) {
        return player != null && pendingInputStore.hasPendingAdd(player.getUniqueId());
    }

    public boolean capturePendingInput(Player player, String message) {
        if (!hasPendingAdd(player)) {
            return false;
        }

        scheduler.runAtPlayer(player, () -> actionService.handleChatInput(player, message));
        return true;
    }

    private static void suppressLegacyChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        event.setMessage("");
        event.getRecipients().clear();
    }

    private static SignLockScheduler schedulerFromConsumer(Consumer<Runnable> nextTick) {
        return new SignLockScheduler() {
            @Override
            public void runNextTick(Runnable task) {
                nextTick.accept(task);
            }

            @Override
            public void runAtPlayer(Player player, Runnable task) {
                nextTick.accept(task);
            }

            @Override
            public void runAtBlock(org.bukkit.block.Block block, Runnable task) {
                nextTick.accept(task);
            }
        };
    }
}
