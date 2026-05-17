package ym.signLock.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LockGuiConfig {

    public static final int DEFAULT_SIZE = 27;
    public static final int DEFAULT_OWNER_SLOT = 0;
    public static final int DEFAULT_TARGET_SLOT = 1;
    public static final int DEFAULT_EXTENSIONS_SLOT = 2;
    public static final int DEFAULT_SCOPE_SLOT = 3;
    public static final int DEFAULT_REMOVE_SELECTED_SLOT = 23;
    public static final int DEFAULT_ADD_SLOT = 24;
    public static final int DEFAULT_REFRESH_SLOT = 25;
    public static final int DEFAULT_CLOSE_SLOT = 26;
    public static final int[] DEFAULT_PLAYER_SLOTS = {
            9, 10, 11, 12, 13, 14, 15,
            16, 17, 18, 19, 20, 21, 22
    };

    private static final List<String> DEFAULT_LAYOUT = List.of(
            "OTES#####",
            "IIIIIIIII",
            "IIIIIRBFC"
    );

    private static final LayoutLegend DEFAULT_LEGEND = new LayoutLegend('O', 'T', 'E', 'S', 'I', 'R', 'B', 'F', 'C');

    private final String title;
    private final int size;
    private final int ownerSlot;
    private final int targetSlot;
    private final int extensionsSlot;
    private final int scopeSlot;
    private final int removeSelectedSlot;
    private final int addSlot;
    private final int refreshSlot;
    private final int closeSlot;
    private final int[] playerSlots;
    private final GuiItemTemplate ownerItem;
    private final GuiItemTemplate targetItem;
    private final GuiItemTemplate extensionsItem;
    private final GuiItemTemplate scopeManageItem;
    private final GuiItemTemplate scopeReadOnlyItem;
    private final GuiItemTemplate playersHiddenItem;
    private final GuiItemTemplate noPlayersItem;
    private final GuiItemTemplate playerItem;
    private final GuiItemTemplate playerSelectedItem;
    private final GuiItemTemplate playerReadOnlyItem;
    private final GuiItemTemplate removeSelectedItem;
    private final GuiItemTemplate addItem;
    private final GuiItemTemplate refreshItem;
    private final GuiItemTemplate closeItem;
    private final GuiItemTemplate readOnlyActionItem;

    public LockGuiConfig(FileConfiguration config) {
        this.title = colorize(config.getString("gui.title", "&8SignLock 管理"));

        LayoutSlots slots = loadLayout(config);
        this.size = slots.size();
        this.ownerSlot = slots.ownerSlot();
        this.targetSlot = slots.targetSlot();
        this.extensionsSlot = slots.extensionsSlot();
        this.scopeSlot = slots.scopeSlot();
        this.removeSelectedSlot = slots.removeSelectedSlot();
        this.addSlot = slots.addSlot();
        this.refreshSlot = slots.refreshSlot();
        this.closeSlot = slots.closeSlot();
        this.playerSlots = slots.playerSlots();

        this.ownerItem = loadItem(config, "gui.items.owner", Material.OAK_SIGN, "&e所有者: &f%owner%");
        this.targetItem = loadItem(config, "gui.items.target", Material.CHEST, "&e保护目标: &f%target%");
        this.extensionsItem = loadItem(config, "gui.items.extensions", Material.NAME_TAG, "&e扩展牌数量: &f%count%");
        this.scopeManageItem = loadItem(config, "gui.items.scope-manage", Material.WRITABLE_BOOK, "&e当前模式: &f%scope%", "&7点击玩家名切换移除选择");
        this.scopeReadOnlyItem = loadItem(config, "gui.items.scope-read-only", Material.BOOK, "&e当前模式: &f%scope%", "&7你当前只能查看，不能修改");
        this.playersHiddenItem = loadItem(config, "gui.items.players-hidden", Material.BARRIER, "&c你无权查看授权名单");
        this.noPlayersItem = loadItem(config, "gui.items.no-players", Material.BARRIER, "&7当前还没有额外授权玩家");
        this.playerItem = loadItem(config, "gui.items.player", Material.PLAYER_HEAD, "&f%player%", "&eShift + 右键移除授权");
        this.playerSelectedItem = loadItem(config, "gui.items.player-selected", Material.PLAYER_HEAD, "&f%player%", "&eShift + 右键移除授权");
        this.playerReadOnlyItem = loadItem(config, "gui.items.player-read-only", Material.PLAYER_HEAD, "&f%player%", "&7只读查看");
        this.removeSelectedItem = loadItem(config, "gui.items.remove-selected", Material.REDSTONE, "&c移除说明", "&7对玩家头颅使用 Shift + 右键即可移除");
        this.addItem = loadItem(config, "gui.items.add", Material.LIME_DYE, "&a添加授权", "&7点击后在聊天栏输入玩家名");
        this.refreshItem = loadItem(config, "gui.items.refresh", Material.CLOCK, "&e刷新界面");
        this.closeItem = loadItem(config, "gui.items.close", Material.BARRIER, "&c关闭");
        this.readOnlyActionItem = loadItem(config, "gui.items.read-only-action", Material.GRAY_STAINED_GLASS_PANE, "&7只读", "&7你当前只能查看，不能修改");
    }

    public String title() {
        return title;
    }

    public int size() {
        return size;
    }

    public int ownerSlot() {
        return ownerSlot;
    }

    public int targetSlot() {
        return targetSlot;
    }

    public int extensionsSlot() {
        return extensionsSlot;
    }

    public int scopeSlot() {
        return scopeSlot;
    }

    public int removeSelectedSlot() {
        return removeSelectedSlot;
    }

    public int addSlot() {
        return addSlot;
    }

    public int refreshSlot() {
        return refreshSlot;
    }

    public int closeSlot() {
        return closeSlot;
    }

    public int[] playerSlots() {
        return playerSlots.clone();
    }

    public ItemStack ownerItem(String owner) {
        return ownerItem.render(Map.of("%owner%", owner));
    }

    public ItemStack targetItem(String target) {
        return targetItem.render(Map.of("%target%", target));
    }

    public ItemStack extensionsItem(int count) {
        return extensionsItem.render(Map.of("%count%", Integer.toString(count)));
    }

    public ItemStack scopeItem(String scope, boolean readOnly) {
        return (readOnly ? scopeReadOnlyItem : scopeManageItem).render(Map.of("%scope%", scope));
    }

    public ItemStack playersHiddenItem() {
        return playersHiddenItem.render(Map.of());
    }

    public ItemStack noPlayersItem() {
        return noPlayersItem.render(Map.of());
    }

    public ItemStack playerItem(String playerName) {
        return playerItem.renderPlayer(Map.of("%player%", playerName), playerName);
    }

    public ItemStack playerSelectedItem(String playerName) {
        return playerSelectedItem.renderPlayer(Map.of("%player%", playerName), playerName);
    }

    public ItemStack playerReadOnlyItem(String playerName) {
        return playerReadOnlyItem.renderPlayer(Map.of("%player%", playerName), playerName);
    }

    public ItemStack removeSelectedItem(int selectedCount) {
        return removeSelectedItem.render(Map.of("%selected_count%", Integer.toString(selectedCount)));
    }

    public ItemStack addItem() {
        return addItem.render(Map.of());
    }

    public ItemStack refreshItem() {
        return refreshItem.render(Map.of());
    }

    public ItemStack closeItem() {
        return closeItem.render(Map.of());
    }

    public ItemStack readOnlyActionItem() {
        return readOnlyActionItem.render(Map.of());
    }

    private LayoutSlots loadLayout(FileConfiguration config) {
        List<String> inlineRows = readLayoutRows(config.get("gui.layout"));
        if (!inlineRows.isEmpty()) {
            LayoutSlots slots = parsePatternLayout(inlineRows, loadLegend(config.getConfigurationSection("gui.legend")));
            if (slots != null) {
                return slots;
            }
        }

        List<String> patternRows = config.getStringList("gui.layout.pattern");
        if (!patternRows.isEmpty()) {
            LayoutSlots slots = parsePatternLayout(patternRows, loadLegend(config.getConfigurationSection("gui.legend")));
            if (slots != null) {
                return slots;
            }
        }

        LayoutSlots defaults = parsePatternLayout(DEFAULT_LAYOUT, DEFAULT_LEGEND);
        if (defaults != null) {
            return defaults;
        }

        return loadLegacySlotLayout(config);
    }

    private LayoutSlots loadLegacySlotLayout(FileConfiguration config) {
        int legacySize = normalizeSize(config.getInt("gui.layout.size", DEFAULT_SIZE));
        Set<Integer> claimed = new LinkedHashSet<>();
        int legacyOwnerSlot = normalizeSlot(config.getInt("gui.layout.owner-slot", DEFAULT_OWNER_SLOT), DEFAULT_OWNER_SLOT, legacySize, claimed);
        int legacyTargetSlot = normalizeSlot(config.getInt("gui.layout.target-slot", DEFAULT_TARGET_SLOT), DEFAULT_TARGET_SLOT, legacySize, claimed);
        int legacyExtensionsSlot = normalizeSlot(config.getInt("gui.layout.extensions-slot", DEFAULT_EXTENSIONS_SLOT), DEFAULT_EXTENSIONS_SLOT, legacySize, claimed);
        int legacyScopeSlot = normalizeSlot(config.getInt("gui.layout.scope-slot", DEFAULT_SCOPE_SLOT), DEFAULT_SCOPE_SLOT, legacySize, claimed);
        int legacyRemoveSlot = normalizeSlot(
                config.getInt("gui.layout.remove-selected-slot", DEFAULT_REMOVE_SELECTED_SLOT),
                DEFAULT_REMOVE_SELECTED_SLOT,
                legacySize,
                claimed
        );
        int legacyAddSlot = normalizeSlot(config.getInt("gui.layout.add-slot", DEFAULT_ADD_SLOT), DEFAULT_ADD_SLOT, legacySize, claimed);
        int legacyRefreshSlot = normalizeSlot(config.getInt("gui.layout.refresh-slot", DEFAULT_REFRESH_SLOT), DEFAULT_REFRESH_SLOT, legacySize, claimed);
        int legacyCloseSlot = normalizeSlot(config.getInt("gui.layout.close-slot", DEFAULT_CLOSE_SLOT), DEFAULT_CLOSE_SLOT, legacySize, claimed);
        int[] legacyPlayerSlots = normalizePlayerSlots(config.getIntegerList("gui.layout.player-slots"), legacySize, claimed);
        return new LayoutSlots(
                legacySize,
                legacyOwnerSlot,
                legacyTargetSlot,
                legacyExtensionsSlot,
                legacyScopeSlot,
                legacyRemoveSlot,
                legacyAddSlot,
                legacyRefreshSlot,
                legacyCloseSlot,
                legacyPlayerSlots
        );
    }

    private LayoutLegend loadLegend(ConfigurationSection section) {
        if (section == null) {
            return DEFAULT_LEGEND;
        }

        return new LayoutLegend(
                charOf(section.getString("owner"), DEFAULT_LEGEND.owner()),
                charOf(section.getString("target"), DEFAULT_LEGEND.target()),
                charOf(section.getString("extensions"), DEFAULT_LEGEND.extensions()),
                charOf(section.getString("scope"), DEFAULT_LEGEND.scope()),
                charOf(section.getString("players"), DEFAULT_LEGEND.players()),
                charOf(section.getString("remove-selected"), DEFAULT_LEGEND.removeSelected()),
                charOf(section.getString("add"), DEFAULT_LEGEND.add()),
                charOf(section.getString("refresh"), DEFAULT_LEGEND.refresh()),
                charOf(section.getString("close"), DEFAULT_LEGEND.close())
        );
    }

    private LayoutSlots parsePatternLayout(List<String> rows, LayoutLegend legend) {
        List<String> normalizedRows = normalizeRows(rows);
        if (normalizedRows.isEmpty()) {
            return null;
        }

        int rowCount = normalizedRows.size();
        if (rowCount > 6) {
            return null;
        }

        int layoutSize = rowCount * 9;
        Set<Integer> claimed = new LinkedHashSet<>();
        List<Integer> playerSlotsFound = new ArrayList<>();
        int owner = -1;
        int target = -1;
        int extensions = -1;
        int scope = -1;
        int removeSelected = -1;
        int add = -1;
        int refresh = -1;
        int close = -1;

        for (int rowIndex = 0; rowIndex < normalizedRows.size(); rowIndex++) {
            String row = normalizedRows.get(rowIndex);
            for (int column = 0; column < 9; column++) {
                char symbol = row.charAt(column);
                if (symbol == '#' || symbol == ' ') {
                    continue;
                }

                int slot = rowIndex * 9 + column;
                if (symbol == legend.players()) {
                    claimed.add(slot);
                    playerSlotsFound.add(slot);
                    continue;
                }

                if (symbol == legend.owner() && owner == -1) {
                    owner = claimSingle(slot, claimed);
                    continue;
                }
                if (symbol == legend.target() && target == -1) {
                    target = claimSingle(slot, claimed);
                    continue;
                }
                if (symbol == legend.extensions() && extensions == -1) {
                    extensions = claimSingle(slot, claimed);
                    continue;
                }
                if (symbol == legend.scope() && scope == -1) {
                    scope = claimSingle(slot, claimed);
                    continue;
                }
                if (symbol == legend.removeSelected() && removeSelected == -1) {
                    removeSelected = claimSingle(slot, claimed);
                    continue;
                }
                if (symbol == legend.add() && add == -1) {
                    add = claimSingle(slot, claimed);
                    continue;
                }
                if (symbol == legend.refresh() && refresh == -1) {
                    refresh = claimSingle(slot, claimed);
                    continue;
                }
                if (symbol == legend.close() && close == -1) {
                    close = claimSingle(slot, claimed);
                }
            }
        }

        owner = resolveParsedSlot(owner, DEFAULT_OWNER_SLOT, layoutSize, claimed);
        target = resolveParsedSlot(target, DEFAULT_TARGET_SLOT, layoutSize, claimed);
        extensions = resolveParsedSlot(extensions, DEFAULT_EXTENSIONS_SLOT, layoutSize, claimed);
        scope = resolveParsedSlot(scope, DEFAULT_SCOPE_SLOT, layoutSize, claimed);
        removeSelected = resolveParsedSlot(removeSelected, DEFAULT_REMOVE_SELECTED_SLOT, layoutSize, claimed);
        add = resolveParsedSlot(add, DEFAULT_ADD_SLOT, layoutSize, claimed);
        refresh = resolveParsedSlot(refresh, DEFAULT_REFRESH_SLOT, layoutSize, claimed);
        close = resolveParsedSlot(close, DEFAULT_CLOSE_SLOT, layoutSize, claimed);
        int[] players = resolveParsedPlayerSlots(playerSlotsFound, layoutSize, claimed);

        return new LayoutSlots(layoutSize, owner, target, extensions, scope, removeSelected, add, refresh, close, players);
    }

    private static int claimSingle(int slot, Set<Integer> claimed) {
        claimed.add(slot);
        return slot;
    }

    private static int resolveParsedSlot(int requested, int fallback, int size, Set<Integer> claimed) {
        if (requested >= 0 && requested < size) {
            return requested;
        }
        return normalizeSlot(requested, fallback, size, claimed);
    }

    private static int[] resolveParsedPlayerSlots(List<Integer> parsedSlots, int size, Set<Integer> claimed) {
        if (parsedSlots != null && !parsedSlots.isEmpty()) {
            return parsedSlots.stream().mapToInt(Integer::intValue).toArray();
        }
        return normalizePlayerSlots(parsedSlots, size, claimed);
    }

    private static List<String> readLayoutRows(Object rawLayout) {
        if (!(rawLayout instanceof List<?> values)) {
            return List.of();
        }

        List<String> rows = new ArrayList<>(values.size());
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            rows.add(value.toString());
        }
        return rows;
    }

    private static List<String> normalizeRows(List<String> rows) {
        List<String> normalized = new ArrayList<>(rows.size());
        for (String row : rows) {
            if (row == null || row.isBlank()) {
                continue;
            }
            if (row.length() != 9) {
                return List.of();
            }
            normalized.add(row);
        }
        return normalized;
    }

    private static GuiItemTemplate loadItem(FileConfiguration config, String path, Material defaultMaterial, String defaultName, String... defaultLore) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return new GuiItemTemplate(defaultMaterial, colorize(defaultName), colorize(List.of(defaultLore)));
        }

        Material material = parseMaterial(section.getString("material"), defaultMaterial);
        String name = colorize(section.getString("name", defaultName));
        List<String> lore = section.contains("lore")
                ? colorize(section.getStringList("lore"))
                : colorize(List.of(defaultLore));
        return new GuiItemTemplate(material, name, lore);
    }

    private static Material parseMaterial(String raw, Material fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Material.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static int normalizeSize(int requested) {
        if (requested < 9 || requested > 54 || requested % 9 != 0) {
            return DEFAULT_SIZE;
        }
        return requested;
    }

    private static int normalizeSlot(int requested, int fallback, int size, Set<Integer> claimed) {
        int candidate = requested;
        if (candidate < 0 || candidate >= size || claimed.contains(candidate)) {
            candidate = fallback;
        }
        if (candidate < 0 || candidate >= size || claimed.contains(candidate)) {
            for (int slot = 0; slot < size; slot++) {
                if (!claimed.contains(slot)) {
                    claimed.add(slot);
                    return slot;
                }
            }
            return 0;
        }
        claimed.add(candidate);
        return candidate;
    }

    private static int[] normalizePlayerSlots(List<Integer> requestedSlots, int size, Set<Integer> claimed) {
        List<Integer> result = new ArrayList<>();
        List<Integer> source = requestedSlots == null || requestedSlots.isEmpty()
                ? ints(DEFAULT_PLAYER_SLOTS)
                : requestedSlots;

        for (Integer rawSlot : source) {
            if (rawSlot == null || rawSlot < 0 || rawSlot >= size || claimed.contains(rawSlot)) {
                continue;
            }
            claimed.add(rawSlot);
            result.add(rawSlot);
        }

        if (result.isEmpty()) {
            for (int fallback : DEFAULT_PLAYER_SLOTS) {
                if (fallback >= 0 && fallback < size && !claimed.contains(fallback)) {
                    claimed.add(fallback);
                    result.add(fallback);
                }
            }
        }

        if (result.isEmpty()) {
            for (int slot = 0; slot < size; slot++) {
                if (!claimed.contains(slot)) {
                    claimed.add(slot);
                    result.add(slot);
                }
            }
        }

        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    private static List<Integer> ints(int[] values) {
        List<Integer> result = new ArrayList<>(values.length);
        for (int value : values) {
            result.add(value);
        }
        return result;
    }

    private static char charOf(String raw, char fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        return raw.trim().charAt(0);
    }

    private static String colorize(String value) {
        return ChatColor.translateAlternateColorCodes('&', value == null ? "" : value);
    }

    private static List<String> colorize(List<String> values) {
        List<String> result = new ArrayList<>(values.size());
        for (String value : values) {
            result.add(colorize(value));
        }
        return result;
    }

    private record LayoutLegend(
            char owner,
            char target,
            char extensions,
            char scope,
            char players,
            char removeSelected,
            char add,
            char refresh,
            char close
    ) {
    }

    private record LayoutSlots(
            int size,
            int ownerSlot,
            int targetSlot,
            int extensionsSlot,
            int scopeSlot,
            int removeSelectedSlot,
            int addSlot,
            int refreshSlot,
            int closeSlot,
            int[] playerSlots
    ) {

        private LayoutSlots {
            playerSlots = playerSlots.clone();
        }
    }

    private record GuiItemTemplate(Material material, String name, List<String> lore) {

        private GuiItemTemplate {
            lore = List.copyOf(lore);
        }

        private ItemStack render(Map<String, String> placeholders) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return item;
            }

            meta.setDisplayName(apply(name, placeholders));
            if (!lore.isEmpty()) {
                List<String> appliedLore = new ArrayList<>(lore.size());
                for (String line : lore) {
                    appliedLore.add(apply(line, placeholders));
                }
                meta.setLore(appliedLore);
            }
            item.setItemMeta(meta);
            return item;
        }

        private ItemStack renderPlayer(Map<String, String> placeholders, String playerName) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return item;
            }

            meta.setDisplayName(apply(name, placeholders));
            if (!lore.isEmpty()) {
                List<String> appliedLore = new ArrayList<>(lore.size());
                for (String line : lore) {
                    appliedLore.add(apply(line, placeholders));
                }
                meta.setLore(appliedLore);
            }
            if (meta instanceof SkullMeta skullMeta && material == Material.PLAYER_HEAD && playerName != null && !playerName.isBlank()) {
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
                meta = skullMeta;
            }
            item.setItemMeta(meta);
            return item;
        }

        private String apply(String source, Map<String, String> placeholders) {
            String result = source;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
            return result;
        }
    }
}
