package ym.signLock.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LockManagementGuiHolder implements InventoryHolder {

    private final LockManagementSession session;
    private final LockSummaryView view;
    private final Map<Integer, String> removablePlayersBySlot = new HashMap<>();
    private Inventory inventory;

    public LockManagementGuiHolder(LockManagementSession session, LockSummaryView view) {
        this.session = session;
        this.view = view;
        List<String> players = view.allowedPlayers();
        for (int index = 0; index < players.size() && index < LockManagementGui.PLAYER_SLOTS.length; index++) {
            removablePlayersBySlot.put(LockManagementGui.PLAYER_SLOTS[index], players.get(index));
        }
    }

    public LockManagementSession session() {
        return session;
    }

    public LockSummaryView view() {
        return view;
    }

    public void bindInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public String removablePlayerAt(int rawSlot) {
        return removablePlayersBySlot.get(rawSlot);
    }
}
