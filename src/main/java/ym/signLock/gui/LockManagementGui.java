package ym.signLock.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ym.signLock.config.SignLockConfig;

import java.util.ArrayList;
import java.util.List;

public final class LockManagementGui {

    public static final int SIZE = 27;
    public static final int OWNER_SLOT = 0;
    public static final int TARGET_SLOT = 1;
    public static final int EXTENSIONS_SLOT = 2;
    public static final int SCOPE_SLOT = 3;
    public static final int REMOVE_SELECTED_SLOT = 23;
    public static final int ADD_SLOT = 24;
    public static final int REFRESH_SLOT = 25;
    public static final int CLOSE_SLOT = 26;
    public static final int[] PLAYER_SLOTS = {
            9, 10, 11, 12, 13, 14, 15,
            16, 17, 18, 19, 20, 21, 22
    };

    private LockManagementGui() {
    }

    public static Inventory createInventory(LockManagementGuiHolder holder, SignLockConfig config) {
        Inventory inventory = Bukkit.createInventory(holder, SIZE, config.guiTitle());
        holder.bindInventory(inventory);

        LockSummaryView view = holder.view();
        LockSummaryTarget target = view.target();

        inventory.setItem(OWNER_SLOT, item(Material.OAK_SIGN, config.guiOwnerLabel(view.owner())));
        inventory.setItem(TARGET_SLOT, item(Material.CHEST, config.guiTargetLabel(target.summaryLabel(config))));
        inventory.setItem(EXTENSIONS_SLOT, item(Material.NAME_TAG, config.guiExtensionsLabel(view.extensionCount())));
        inventory.setItem(
                SCOPE_SLOT,
                item(
                        view.readOnly() ? Material.BOOK : Material.WRITABLE_BOOK,
                        config.guiScopeLabel(view.scopeLabel(config)),
                        view.readOnly() ? config.guiReadOnlyHintLine() : config.guiRemoveHintLine()
                )
        );

        if (!view.canViewAuthorizedPlayers()) {
            inventory.setItem(PLAYER_SLOTS[0], item(Material.BARRIER, config.infoPlayersHiddenMessage()));
        } else if (view.allowedPlayers().isEmpty()) {
            inventory.setItem(PLAYER_SLOTS[0], item(Material.BARRIER, config.guiNoPlayersLabel()));
        } else {
            List<String> players = view.allowedPlayers();
            for (int index = 0; index < players.size() && index < PLAYER_SLOTS.length; index++) {
                int slot = PLAYER_SLOTS[index];
                boolean selected = holder.isSelected(slot);
                Material playerItem = view.readOnly() ? Material.PAPER : (selected ? Material.RED_DYE : Material.PAPER);
                String[] lore = view.readOnly()
                        ? new String[]{config.guiPlayerReadOnlyHintLine()}
                        : new String[]{config.guiRemoveHintLine(), selected ? config.guiSelectedHintLine() : config.guiSelectHintLine()};
                inventory.setItem(
                        slot,
                        item(
                                playerItem,
                                players.get(index),
                                lore
                        )
                );
            }
        }

        if (view.readOnly()) {
            inventory.setItem(
                    REMOVE_SELECTED_SLOT,
                    item(Material.GRAY_STAINED_GLASS_PANE, config.guiReadOnlyButtonLabel(), config.guiReadOnlyHintLine())
            );
            inventory.setItem(
                    ADD_SLOT,
                    item(Material.GRAY_STAINED_GLASS_PANE, config.guiReadOnlyButtonLabel(), config.guiReadOnlyHintLine())
            );
        } else {
            inventory.setItem(
                    REMOVE_SELECTED_SLOT,
                    item(Material.REDSTONE, config.guiRemoveSelectedButtonLabel(holder.selectedCount()), config.guiRemoveSelectedHintLine())
            );
            inventory.setItem(ADD_SLOT, item(Material.LIME_DYE, config.guiAddButtonLabel()));
        }
        inventory.setItem(REFRESH_SLOT, item(Material.CLOCK, config.guiRefreshButtonLabel()));
        inventory.setItem(CLOSE_SLOT, item(Material.BARRIER, config.guiCloseButtonLabel()));
        return inventory;
    }

    private static ItemStack item(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.setDisplayName(name);
        if (loreLines.length > 0) {
            List<String> lore = new ArrayList<>(loreLines.length);
            for (String line : loreLines) {
                lore.add(line);
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }
}
