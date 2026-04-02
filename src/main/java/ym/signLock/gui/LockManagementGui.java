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
        inventory.setItem(TARGET_SLOT, item(
                Material.CHEST,
                config.guiTargetLabel(target.blockType(), target.worldName(), target.x(), target.y(), target.z())
        ));
        inventory.setItem(EXTENSIONS_SLOT, item(Material.NAME_TAG, config.guiExtensionsLabel(view.extensionCount())));

        if (view.allowedPlayers().isEmpty()) {
            inventory.setItem(PLAYER_SLOTS[0], item(Material.BARRIER, config.guiNoPlayersLabel()));
        } else {
            List<String> players = view.allowedPlayers();
            for (int index = 0; index < players.size() && index < PLAYER_SLOTS.length; index++) {
                inventory.setItem(
                        PLAYER_SLOTS[index],
                        item(Material.PAPER, players.get(index), config.guiRemoveHintLine())
                );
            }
        }

        inventory.setItem(ADD_SLOT, item(Material.LIME_DYE, config.guiAddButtonLabel()));
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
