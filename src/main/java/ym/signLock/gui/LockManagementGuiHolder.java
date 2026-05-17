package ym.signLock.gui;

import ym.signLock.config.LockGuiConfig;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LockManagementGuiHolder implements InventoryHolder {

    private final LockManagementSession session;
    private final LockSummaryView view;
    private final LockGuiConfig guiConfig;
    private final Map<Integer, String> removablePlayersBySlot = new HashMap<>();
    private final Set<String> selectedPlayers = new LinkedHashSet<>();
    private Inventory inventory;

    public LockManagementGuiHolder(LockManagementSession session, LockSummaryView view) {
        this(session, view, new LockGuiConfig(new org.bukkit.configuration.file.YamlConfiguration()));
    }

    public LockManagementGuiHolder(LockManagementSession session, LockSummaryView view, LockGuiConfig guiConfig) {
        this.session = session;
        this.view = view;
        this.guiConfig = guiConfig;
        if (view.canManage()) {
            List<String> players = view.allowedPlayers();
            int[] playerSlots = guiConfig.playerSlots();
            for (int index = 0; index < players.size() && index < playerSlots.length; index++) {
                removablePlayersBySlot.put(playerSlots[index], players.get(index));
            }
        }
    }

    public LockManagementSession session() {
        return session;
    }

    public LockSummaryView view() {
        return view;
    }

    public LockGuiConfig guiConfig() {
        return guiConfig;
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

    public boolean readOnly() {
        return view.readOnly();
    }

    public void toggleSelectedPlayer(int rawSlot) {
        String playerName = removablePlayerAt(rawSlot);
        if (playerName == null) {
            return;
        }

        if (!selectedPlayers.add(playerName)) {
            selectedPlayers.remove(playerName);
        }
    }

    public boolean isSelected(int rawSlot) {
        String playerName = removablePlayerAt(rawSlot);
        return playerName != null && selectedPlayers.contains(playerName);
    }

    public Set<String> selectedPlayers() {
        return Set.copyOf(selectedPlayers);
    }

    public int selectedCount() {
        return selectedPlayers.size();
    }

    public boolean hasSelection() {
        return !selectedPlayers.isEmpty();
    }
}
