package ym.signLock.config;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SignLockConfig {

    private final FileConfiguration backingConfig;
    private final String lockHeader;
    private final String moreUsersHeader;
    private final boolean protectExplosions;
    private final boolean adminBypass;
    private final int maxMoreUserSigns;
    private final List<BlockFace> extensionPlacementOrder;
    private final Set<Material> lockableMaterials;
    private final String lockedUseMessage;
    private final String lockedContainerMessage;
    private final String protectedBlockMessage;
    private final String protectedSignMessage;
    private final String invalidLockPlacementMessage;
    private final String alreadyProtectedMessage;
    private final String lockCreatedMessage;
    private final String lockUsageHintMessage;
    private final String invalidMoreUsersPlacementMessage;
    private final String missingPrimaryLockMessage;
    private final String ownerOnlyMoreUsersMessage;
    private final String extraUsersAttachedMessage;
    private final String reloadSuccessMessage;
    private final String reloadUsageMessage;
    private final String noPermissionMessage;
    private final String addUsageMessage;
    private final String removeUsageMessage;
    private final String infoUsageMessage;
    private final String addSuccessMessage;
    private final String addAlreadyAuthorizedMessage;
    private final String addListFullMessage;
    private final String addInvalidSignMessage;
    private final String addOnlyOwnerMessage;
    private final String addPlayerNotFoundMessage;
    private final String removeSuccessMessage;
    private final String removeNotFoundMessage;
    private final String removeOwnerDeniedMessage;
    private final String directLockNoSpaceMessage;
    private final String infoHeaderMessage;
    private final String infoOwnerMessage;
    private final String infoPlayersMessage;
    private final String infoNoPlayersMessage;
    private final String infoExtensionsMessage;
    private final String automationBlockedMessage;
    private final String signEditDeniedMessage;
    private final String extensionCreatedMessage;
    private final String guiTitle;
    private final String guiOwnerLabel;
    private final String guiTargetLabel;
    private final String guiExtensionsLabel;
    private final String guiNoPlayersLabel;
    private final String guiAddButtonLabel;
    private final String guiRefreshButtonLabel;
    private final String guiCloseButtonLabel;
    private final String guiRemoveHintLine;
    private final String guiSelectHintLine;
    private final String guiSelectedHintLine;
    private final String guiRemoveSelectedButtonLabel;
    private final String guiRemoveSelectedHintLine;
    private final String guiAddPromptMessage;
    private final String guiAddCancelledMessage;
    private final String guiRemoveSelectionEmptyMessage;
    private final String batchAddSummaryMessage;
    private final String batchRemoveSummaryMessage;

    public SignLockConfig(FileConfiguration config) {
        this.backingConfig = config;
        this.lockHeader = normalizeHeader(config.getString("signs.lock-header", "[锁]"));
        this.moreUsersHeader = normalizeHeader(config.getString("signs.more-users-header", "[更多用户]"));
        this.protectExplosions = config.getBoolean("protection.explosions", true);
        this.adminBypass = config.getBoolean("protection.admin-bypass", true);
        this.maxMoreUserSigns = Math.max(0, config.getInt("protection.max-more-user-signs", 4));
        this.extensionPlacementOrder = loadPlacementOrder(config.getStringList("protection.extension-placement-order"));
        this.lockableMaterials = loadLockableMaterials(config.getStringList("protection.lockable-materials"));

        this.lockedUseMessage = message("messages.locked-use", "&c你没有权限使用这个已上锁的方块。");
        this.lockedContainerMessage = message("messages.locked-container", "&c你没有权限打开这个已上锁的容器。");
        this.protectedBlockMessage = message("messages.protected-block", "&c这个方块正受到牌子锁保护。");
        this.protectedSignMessage = message("messages.protected-sign", "&c你不能破坏别人的锁牌。");
        this.invalidLockPlacementMessage = message("messages.invalid-lock-placement", "&c请把牌子放在可上锁方块旁边。");
        this.alreadyProtectedMessage = message("messages.already-protected", "&c这个方块已经上锁了。");
        this.lockCreatedMessage = message("messages.lock-created", "&a已为 &f%block% &a创建牌子锁。");
        this.lockUsageHintMessage = message("messages.lock-usage-hint", "&7右键自己的锁牌可管理权限，或使用 /bl add 与 /bl remove。");
        this.invalidMoreUsersPlacementMessage = message("messages.invalid-more-users-placement", "&c请把牌子放在已上锁方块旁边。");
        this.missingPrimaryLockMessage = message("messages.missing-primary-lock", "&c[更多用户] 牌子必须依附在已有主锁的方块上。");
        this.ownerOnlyMoreUsersMessage = message("messages.owner-only-more-users", "&c只有锁主人才能添加额外授权牌。");
        this.extraUsersAttachedMessage = message("messages.extra-users-attached", "&a已添加额外授权牌。");
        this.reloadSuccessMessage = message("messages.reload-success", "&aSignLock 配置已重载。");
        this.reloadUsageMessage = message("messages.reload-usage", "&e用法: /signlock reload");
        this.noPermissionMessage = message("messages.no-permission", "&c你没有权限执行这个操作。");
        this.addUsageMessage = message("messages.add-usage", "&e用法: /bl add <玩家名>");
        this.removeUsageMessage = message("messages.remove-usage", "&e用法: /bl remove <玩家名>");
        this.infoUsageMessage = message("messages.info-usage", "&e用法: /bl info");
        this.addSuccessMessage = message("messages.add-success", "&a已将 &f%player% &a加入这把锁。");
        this.addAlreadyAuthorizedMessage = message("messages.add-already-authorized", "&e%player% 已经拥有这把锁的权限。");
        this.addListFullMessage = message("messages.add-list-full", "&c授权牌已满，无法继续扩展。");
        this.addInvalidSignMessage = message("messages.add-invalid-sign", "&c请把准星对准一个有效的锁牌。");
        this.addOnlyOwnerMessage = message("messages.add-only-owner", "&c只有锁主人才能修改这把锁的授权。");
        this.addPlayerNotFoundMessage = message("messages.add-player-not-found", "&c请输入要添加的玩家名。");
        this.removeSuccessMessage = message("messages.remove-success", "&a已从这把锁中移除 &f%player%&a。");
        this.removeNotFoundMessage = message("messages.remove-not-found", "&e这把锁里没有玩家 &f%player%&e。");
        this.removeOwnerDeniedMessage = message("messages.remove-owner-denied", "&c不能移除锁的主人。");
        this.directLockNoSpaceMessage = message("messages.direct-lock-no-space", "&c附近没有可放置锁牌的位置，请腾出一个侧面空间。");
        this.infoHeaderMessage = message("messages.info-header", "&6[SignLock] 锁信息");
        this.infoOwnerMessage = message("messages.info-owner", "&e所有者: &f%owner%");
        this.infoPlayersMessage = message("messages.info-players", "&e授权玩家: &f%players%");
        this.infoNoPlayersMessage = message("messages.info-no-players", "&e授权玩家: &f无");
        this.infoExtensionsMessage = message("messages.info-extensions", "&e扩展牌数量: &f%count%");
        this.automationBlockedMessage = message("messages.automation-blocked", "&c自动化装置无法操作已上锁容器。");
        this.signEditDeniedMessage = message("messages.sign-edit-denied", "&c你不能修改别人的锁牌内容。");
        this.extensionCreatedMessage = message("messages.extension-created", "&a已自动生成新的扩展牌。");
        this.guiTitle = message("messages.gui-title", "&8SignLock 管理");
        this.guiOwnerLabel = message("messages.gui-owner-label", "&e所有者: &f%owner%");
        this.guiTargetLabel = message("messages.gui-target-label", "&e保护目标: &f%block% &7(%world% %x%, %y%, %z%)");
        this.guiExtensionsLabel = message("messages.gui-extensions-label", "&e扩展牌数量: &f%count%");
        this.guiNoPlayersLabel = message("messages.gui-no-players-label", "&7当前还没有额外授权玩家");
        this.guiAddButtonLabel = message("messages.gui-add-button", "&a添加授权");
        this.guiRefreshButtonLabel = message("messages.gui-refresh-button", "&e刷新界面");
        this.guiCloseButtonLabel = message("messages.gui-close-button", "&c关闭");
        this.guiRemoveHintLine = message("messages.gui-remove-hint-line", "&7点击切换是否加入批量移除");
        this.guiSelectHintLine = message("messages.gui-select-hint-line", "&e点击选择");
        this.guiSelectedHintLine = message("messages.gui-selected-hint-line", "&c已选中，再点一次可取消");
        this.guiRemoveSelectedButtonLabel = message("messages.gui-remove-selected-button", "&c移除选中(&f%count%&c)");
        this.guiRemoveSelectedHintLine = message("messages.gui-remove-selected-hint-line", "&7确认移除当前已选中的玩家");
        this.guiAddPromptMessage = message("messages.gui-add-prompt", "&e在聊天栏输入要添加的玩家名，可用空格或逗号分隔；输入 cancel 取消。");
        this.guiAddCancelledMessage = message("messages.gui-add-cancelled", "&7已取消添加授权。");
        this.guiRemoveSelectionEmptyMessage = message("messages.gui-remove-selection-empty", "&e请先选中至少一名玩家。");
        this.batchAddSummaryMessage = message("messages.batch-add-summary", "&a批量添加完成: &f成功 %added_count%(%added%) &7| 已有权限 %already_count%(%already%) &7| 无空位 %no_space_count%(%no_space%)");
        this.batchRemoveSummaryMessage = message("messages.batch-remove-summary", "&a批量移除完成: &f成功 %removed_count%(%removed%) &7| 未找到 %not_found_count%(%not_found%) &7| 不可移除 %owner_denied_count%(%owner_denied%)");
    }

    public String lockHeader() {
        return lockHeader;
    }

    public String moreUsersHeader() {
        return moreUsersHeader;
    }

    public boolean protectExplosions() {
        return protectExplosions;
    }

    public boolean adminBypass() {
        return adminBypass;
    }

    public int maxMoreUserSigns() {
        return maxMoreUserSigns;
    }

    public List<BlockFace> extensionPlacementOrder() {
        return extensionPlacementOrder;
    }

    public boolean isLockable(Material material) {
        return lockableMaterials.contains(material);
    }

    public String lockedUseMessage() {
        return lockedUseMessage;
    }

    public String lockedContainerMessage() {
        return lockedContainerMessage;
    }

    public String protectedBlockMessage() {
        return protectedBlockMessage;
    }

    public String protectedSignMessage() {
        return protectedSignMessage;
    }

    public String invalidLockPlacementMessage() {
        return invalidLockPlacementMessage;
    }

    public String alreadyProtectedMessage() {
        return alreadyProtectedMessage;
    }

    public String lockCreatedMessage(String blockName) {
        return lockCreatedMessage.replace("%block%", blockName);
    }

    public String lockUsageHintMessage() {
        return lockUsageHintMessage;
    }

    public String invalidMoreUsersPlacementMessage() {
        return invalidMoreUsersPlacementMessage;
    }

    public String missingPrimaryLockMessage() {
        return missingPrimaryLockMessage;
    }

    public String ownerOnlyMoreUsersMessage() {
        return ownerOnlyMoreUsersMessage;
    }

    public String extraUsersAttachedMessage() {
        return extraUsersAttachedMessage;
    }

    public String reloadSuccessMessage() {
        return reloadSuccessMessage;
    }

    public String reloadUsageMessage() {
        return reloadUsageMessage;
    }

    public String noPermissionMessage() {
        return noPermissionMessage;
    }

    public String addUsageMessage() {
        return addUsageMessage;
    }

    public String removeUsageMessage() {
        return removeUsageMessage;
    }

    public String infoUsageMessage() {
        return infoUsageMessage;
    }

    public String addSuccessMessage(String playerName) {
        return addSuccessMessage.replace("%player%", playerName);
    }

    public String addAlreadyAuthorizedMessage(String playerName) {
        return addAlreadyAuthorizedMessage.replace("%player%", playerName);
    }

    public String addListFullMessage() {
        return addListFullMessage;
    }

    public String addInvalidSignMessage() {
        return addInvalidSignMessage;
    }

    public String addOnlyOwnerMessage() {
        return addOnlyOwnerMessage;
    }

    public String addPlayerNotFoundMessage() {
        return addPlayerNotFoundMessage;
    }

    public String removeSuccessMessage(String playerName) {
        return removeSuccessMessage.replace("%player%", playerName);
    }

    public String removeNotFoundMessage(String playerName) {
        return removeNotFoundMessage.replace("%player%", playerName);
    }

    public String removeOwnerDeniedMessage() {
        return removeOwnerDeniedMessage;
    }

    public String directLockNoSpaceMessage() {
        return directLockNoSpaceMessage;
    }

    public String infoHeaderMessage() {
        return infoHeaderMessage;
    }

    public String infoOwnerMessage(String owner) {
        return infoOwnerMessage.replace("%owner%", owner);
    }

    public String infoPlayersMessage(String players) {
        return infoPlayersMessage.replace("%players%", players);
    }

    public String infoNoPlayersMessage() {
        return infoNoPlayersMessage;
    }

    public String infoPlayersHiddenMessage() {
        return message("messages.info-players-hidden", "&e授权玩家: &7仅对已授权玩家可见");
    }

    public String infoExtensionsMessage(int count) {
        return infoExtensionsMessage.replace("%count%", Integer.toString(count));
    }

    public String infoScopeMessage(String scope) {
        return message("messages.info-scope", "&e当前权限: &f%scope%").replace("%scope%", scope);
    }

    public String infoTargetMessage(String target) {
        return message("messages.info-target", "&e保护目标: &f%target%").replace("%target%", target);
    }

    public String automationBlockedMessage() {
        return automationBlockedMessage;
    }

    public String signEditDeniedMessage() {
        return signEditDeniedMessage;
    }

    public String extensionCreatedMessage() {
        return extensionCreatedMessage;
    }

    public String guiTitle() {
        return guiTitle;
    }

    public String guiOwnerLabel(String owner) {
        return guiOwnerLabel.replace("%owner%", owner);
    }

    public String guiScopeLabel(String scope) {
        return message("messages.gui-scope-label", "&e当前权限: &f%scope%").replace("%scope%", scope);
    }

    public String guiTargetLabel(String targetSummary) {
        return message("messages.gui-target-summary-label", "&e保护目标: &f%target%").replace("%target%", targetSummary);
    }

    public String guiTargetLabel(String blockType, String worldName, int x, int y, int z) {
        return guiTargetLabel
                .replace("%block%", blockType)
                .replace("%world%", worldName)
                .replace("%x%", Integer.toString(x))
                .replace("%y%", Integer.toString(y))
                .replace("%z%", Integer.toString(z));
    }

    public String guiExtensionsLabel(int count) {
        return guiExtensionsLabel.replace("%count%", Integer.toString(count));
    }

    public String guiNoPlayersLabel() {
        return guiNoPlayersLabel;
    }

    public String guiAddButtonLabel() {
        return guiAddButtonLabel;
    }

    public String guiRefreshButtonLabel() {
        return guiRefreshButtonLabel;
    }

    public String guiCloseButtonLabel() {
        return guiCloseButtonLabel;
    }

    public String guiReadOnlyButtonLabel() {
        return message("messages.gui-read-only-button", "&7只读摘要");
    }

    public String guiRemoveHintLine() {
        return guiRemoveHintLine;
    }

    public String guiSelectHintLine() {
        return guiSelectHintLine;
    }

    public String guiSelectedHintLine() {
        return guiSelectedHintLine;
    }

    public String guiReadOnlyHintLine() {
        return message("messages.gui-read-only-hint-line", "&7你可以查看这把锁，但不能修改授权。");
    }

    public String guiPlayerReadOnlyHintLine() {
        return message("messages.gui-player-read-only-hint-line", "&7只读显示");
    }

    public String guiReadOnlyMessage() {
        return message("messages.gui-read-only-message", "&7这个摘要面板是只读的。");
    }

    public String guiRemoveSelectedButtonLabel(int count) {
        return guiRemoveSelectedButtonLabel.replace("%count%", Integer.toString(count));
    }

    public String guiRemoveSelectedHintLine() {
        return guiRemoveSelectedHintLine;
    }

    public String guiAddPromptMessage() {
        return guiAddPromptMessage;
    }

    public String guiAddCancelledMessage() {
        return guiAddCancelledMessage;
    }

    public String guiRemoveSelectionEmptyMessage() {
        return guiRemoveSelectionEmptyMessage;
    }

    public String batchAddSummaryMessage(List<String> addedPlayers, List<String> alreadyAuthorizedPlayers, List<String> noSpacePlayers) {
        return simplifyBatchSummary(batchAddSummaryMessage
                .replace("%added_count%", Integer.toString(addedPlayers.size()))
                .replace("%added%", formatPlayers(addedPlayers))
                .replace("%already_count%", Integer.toString(alreadyAuthorizedPlayers.size()))
                .replace("%already%", formatPlayers(alreadyAuthorizedPlayers))
                .replace("%no_space_count%", Integer.toString(noSpacePlayers.size()))
                .replace("%no_space%", formatPlayers(noSpacePlayers)));
    }

    public String batchRemoveSummaryMessage(List<String> removedPlayers, List<String> notFoundPlayers, List<String> ownerDeniedPlayers) {
        return simplifyBatchSummary(batchRemoveSummaryMessage
                .replace("%removed_count%", Integer.toString(removedPlayers.size()))
                .replace("%removed%", formatPlayers(removedPlayers))
                .replace("%not_found_count%", Integer.toString(notFoundPlayers.size()))
                .replace("%not_found%", formatPlayers(notFoundPlayers))
                .replace("%owner_denied_count%", Integer.toString(ownerDeniedPlayers.size()))
                .replace("%owner_denied%", formatPlayers(ownerDeniedPlayers)));
    }

    public String scopeManageLabel() {
        return message("messages.scope-manage-label", "&a可管理");
    }

    public String scopeAccessLabel() {
        return message("messages.scope-access-label", "&e可访问");
    }

    public String scopeDeniedLabel() {
        return message("messages.scope-denied-label", "&c未授权");
    }

    public String summarySingleChestTargetLabel(String worldName, int x, int y, int z) {
        return formatTargetSummary(
                backingConfig.getString("messages.target-summary-single-chest", "单箱 &7(%world% %x%, %y%, %z%)"),
                "single chest",
                worldName,
                x,
                y,
                z
        );
    }

    public String summaryDoubleChestTargetLabel(String worldName, int x, int y, int z) {
        return formatTargetSummary(
                backingConfig.getString("messages.target-summary-double-chest", "双箱共享锁 &7(%world% %x%, %y%, %z%)"),
                "double chest",
                worldName,
                x,
                y,
                z
        );
    }

    public String summaryGenericTargetLabel(String blockType, String worldName, int x, int y, int z) {
        return formatTargetSummary(
                backingConfig.getString("messages.target-summary-generic", "%block% &7(%world% %x%, %y%, %z%)"),
                blockType,
                worldName,
                x,
                y,
                z
        );
    }

    private String message(String path, String fallback) {
        return color(backingConfig.getString(path, fallback));
    }

    private static String normalizeHeader(String value) {
        String stripped = ChatColor.stripColor(value);
        return stripped == null ? "" : stripped.trim();
    }

    private static String color(String value) {
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    private static String formatPlayers(List<String> players) {
        return players == null || players.isEmpty() ? "无" : String.join(", ", players);
    }

    private static String formatTargetSummary(String template, String blockType, String worldName, int x, int y, int z) {
        return color(template)
                .replace("%block%", blockType)
                .replace("%world%", worldName)
                .replace("%x%", Integer.toString(x))
                .replace("%y%", Integer.toString(y))
                .replace("%z%", Integer.toString(z));
    }

    private static String simplifyBatchSummary(String message) {
        return message.replaceAll("\\b\\d+\\(([^)]*)\\)", "$1");
    }

    private static Set<Material> loadLockableMaterials(List<String> configured) {
        Set<Material> materials = EnumSet.noneOf(Material.class);
        List<String> source = configured == null || configured.isEmpty() ? defaultLockableMaterials() : configured;
        for (String entry : source) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            try {
                materials.add(Material.valueOf(entry.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return materials;
    }

    private static List<BlockFace> loadPlacementOrder(List<String> configured) {
        List<BlockFace> faces = new ArrayList<>();
        List<String> source = configured == null || configured.isEmpty()
                ? List.of("NORTH", "SOUTH", "EAST", "WEST", "UP", "DOWN")
                : configured;
        for (String entry : source) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            try {
                BlockFace face = BlockFace.valueOf(entry.trim().toUpperCase(Locale.ROOT));
                if (!faces.contains(face)) {
                    faces.add(face);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (faces.isEmpty()) {
            faces.addAll(List.of(
                    BlockFace.NORTH,
                    BlockFace.SOUTH,
                    BlockFace.EAST,
                    BlockFace.WEST,
                    BlockFace.UP,
                    BlockFace.DOWN
            ));
        }
        return faces;
    }

    private static List<String> defaultLockableMaterials() {
        return List.of(
                "CHEST",
                "TRAPPED_CHEST",
                "BARREL",
                "FURNACE",
                "BLAST_FURNACE",
                "SMOKER",
                "HOPPER",
                "DROPPER",
                "DISPENSER",
                "SHULKER_BOX",
                "WHITE_SHULKER_BOX",
                "ORANGE_SHULKER_BOX",
                "MAGENTA_SHULKER_BOX",
                "LIGHT_BLUE_SHULKER_BOX",
                "YELLOW_SHULKER_BOX",
                "LIME_SHULKER_BOX",
                "PINK_SHULKER_BOX",
                "GRAY_SHULKER_BOX",
                "LIGHT_GRAY_SHULKER_BOX",
                "CYAN_SHULKER_BOX",
                "PURPLE_SHULKER_BOX",
                "BLUE_SHULKER_BOX",
                "BROWN_SHULKER_BOX",
                "GREEN_SHULKER_BOX",
                "RED_SHULKER_BOX",
                "BLACK_SHULKER_BOX",
                "OAK_DOOR",
                "SPRUCE_DOOR",
                "BIRCH_DOOR",
                "JUNGLE_DOOR",
                "ACACIA_DOOR",
                "DARK_OAK_DOOR",
                "MANGROVE_DOOR",
                "CHERRY_DOOR",
                "BAMBOO_DOOR",
                "CRIMSON_DOOR",
                "WARPED_DOOR",
                "PALE_OAK_DOOR",
                "IRON_DOOR",
                "OAK_TRAPDOOR",
                "SPRUCE_TRAPDOOR",
                "BIRCH_TRAPDOOR",
                "JUNGLE_TRAPDOOR",
                "ACACIA_TRAPDOOR",
                "DARK_OAK_TRAPDOOR",
                "MANGROVE_TRAPDOOR",
                "CHERRY_TRAPDOOR",
                "BAMBOO_TRAPDOOR",
                "CRIMSON_TRAPDOOR",
                "WARPED_TRAPDOOR",
                "PALE_OAK_TRAPDOOR",
                "IRON_TRAPDOOR",
                "OAK_FENCE_GATE",
                "SPRUCE_FENCE_GATE",
                "BIRCH_FENCE_GATE",
                "JUNGLE_FENCE_GATE",
                "ACACIA_FENCE_GATE",
                "DARK_OAK_FENCE_GATE",
                "MANGROVE_FENCE_GATE",
                "CHERRY_FENCE_GATE",
                "BAMBOO_FENCE_GATE",
                "CRIMSON_FENCE_GATE",
                "WARPED_FENCE_GATE",
                "PALE_OAK_FENCE_GATE"
        );
    }
}
