package ym.signLock.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import ym.signLock.config.LockGuiConfig;
import ym.signLock.config.SignLockConfig;

import java.util.List;

public final class LockManagementGui {

    public static final int SIZE = LockGuiConfig.DEFAULT_SIZE;
    public static final int OWNER_SLOT = LockGuiConfig.DEFAULT_OWNER_SLOT;
    public static final int TARGET_SLOT = LockGuiConfig.DEFAULT_TARGET_SLOT;
    public static final int EXTENSIONS_SLOT = LockGuiConfig.DEFAULT_EXTENSIONS_SLOT;
    public static final int SCOPE_SLOT = LockGuiConfig.DEFAULT_SCOPE_SLOT;
    public static final int REMOVE_SELECTED_SLOT = LockGuiConfig.DEFAULT_REMOVE_SELECTED_SLOT;
    public static final int ADD_SLOT = LockGuiConfig.DEFAULT_ADD_SLOT;
    public static final int REFRESH_SLOT = LockGuiConfig.DEFAULT_REFRESH_SLOT;
    public static final int CLOSE_SLOT = LockGuiConfig.DEFAULT_CLOSE_SLOT;
    public static final int[] PLAYER_SLOTS = LockGuiConfig.DEFAULT_PLAYER_SLOTS.clone();

    private LockManagementGui() {
    }

    public static Inventory createInventory(LockManagementGuiHolder holder, SignLockConfig config, LockGuiConfig guiConfig) {
        Inventory inventory = Bukkit.createInventory(holder, guiConfig.size(), guiConfig.title());
        holder.bindInventory(inventory);

        LockSummaryView view = holder.view();
        LockSummaryTarget target = view.target();

        inventory.setItem(guiConfig.ownerSlot(), guiConfig.ownerItem(view.owner()));
        inventory.setItem(guiConfig.targetSlot(), guiConfig.targetItem(target.summaryLabel(config)));
        inventory.setItem(guiConfig.extensionsSlot(), guiConfig.extensionsItem(view.extensionCount()));
        inventory.setItem(
                guiConfig.scopeSlot(),
                guiConfig.scopeItem(view.scopeLabel(config), view.readOnly())
        );

        int[] playerSlots = guiConfig.playerSlots();
        if (!view.canViewAuthorizedPlayers()) {
            inventory.setItem(playerSlots[0], guiConfig.playersHiddenItem());
        } else if (view.allowedPlayers().isEmpty()) {
            inventory.setItem(playerSlots[0], guiConfig.noPlayersItem());
        } else {
            List<String> players = view.allowedPlayers();
            for (int index = 0; index < players.size() && index < playerSlots.length; index++) {
                int slot = playerSlots[index];
                inventory.setItem(
                        slot,
                        view.readOnly()
                                ? guiConfig.playerReadOnlyItem(players.get(index))
                                : guiConfig.playerItem(players.get(index))
                );
            }
        }

        if (view.readOnly()) {
            inventory.setItem(guiConfig.addSlot(), guiConfig.readOnlyActionItem());
        } else {
            inventory.setItem(guiConfig.addSlot(), guiConfig.addItem());
        }
        inventory.setItem(guiConfig.refreshSlot(), guiConfig.refreshItem());
        inventory.setItem(guiConfig.closeSlot(), guiConfig.closeItem());
        return inventory;
    }
}
