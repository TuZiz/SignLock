package ym.signLock.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import ym.signLock.gui.LockManagementGuiActionService;
import ym.signLock.gui.LockManagementGuiHolder;
import ym.signLock.platform.SignLockScheduler;

import java.util.function.Consumer;

public final class LockGuiListener implements Listener {

    private final SignLockScheduler scheduler;
    private final LockManagementGuiActionService actionService;

    public LockGuiListener(Consumer<Runnable> nextTick, LockManagementGuiActionService actionService) {
        this(schedulerFromConsumer(nextTick), actionService);
    }

    public LockGuiListener(SignLockScheduler scheduler, LockManagementGuiActionService actionService) {
        this.scheduler = scheduler;
        this.actionService = actionService;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder() instanceof LockManagementGuiHolder holder)) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= topInventory.getSize()) {
            return;
        }

        boolean rightClick = event.isRightClick();
        boolean shiftClick = event.isShiftClick();
        scheduler.runAtPlayer(player, () -> actionService.handleClick(player, holder, rawSlot, rightClick, shiftClick));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder() instanceof LockManagementGuiHolder)) {
            return;
        }

        int topSize = topInventory.getSize();
        if (event.getRawSlots().stream().anyMatch(slot -> slot < topSize)) {
            event.setCancelled(true);
        }
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
