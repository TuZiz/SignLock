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

    public SignLockConfig(FileConfiguration config) {
        this.lockHeader = normalizeHeader(config.getString("signs.lock-header", "[锁]"));
        this.moreUsersHeader = normalizeHeader(config.getString("signs.more-users-header", "[更多用户]"));
        this.protectExplosions = config.getBoolean("protection.explosions", true);
        this.adminBypass = config.getBoolean("protection.admin-bypass", true);
        this.maxMoreUserSigns = Math.max(0, config.getInt("protection.max-more-user-signs", 4));
        this.extensionPlacementOrder = loadPlacementOrder(config.getStringList("protection.extension-placement-order"));
        this.lockableMaterials = loadLockableMaterials(config.getStringList("protection.lockable-materials"));
        this.lockedUseMessage = color(config.getString("messages.locked-use", "&c你无权使用这个已上锁的方块。"));
        this.lockedContainerMessage = color(config.getString("messages.locked-container", "&c你无权打开这个已上锁的容器。"));
        this.protectedBlockMessage = color(config.getString("messages.protected-block", "&c这个方块已被牌子锁保护。"));
        this.protectedSignMessage = color(config.getString("messages.protected-sign", "&c你不能破坏别人的锁牌。"));
        this.invalidLockPlacementMessage = color(config.getString("messages.invalid-lock-placement", "&c请把牌子放在可上锁方块旁边。"));
        this.alreadyProtectedMessage = color(config.getString("messages.already-protected", "&c这个方块已经上锁了。"));
        this.lockCreatedMessage = color(config.getString("messages.lock-created", "&a已为 &f%block% &a创建牌子锁。"));
        this.lockUsageHintMessage = color(config.getString("messages.lock-usage-hint", "&7可直接右键自己的锁牌编辑第 3、4 行，或使用 /bl add 添加玩家。"));
        this.invalidMoreUsersPlacementMessage = color(config.getString("messages.invalid-more-users-placement", "&c请把牌子放在已上锁方块旁边。"));
        this.missingPrimaryLockMessage = color(config.getString("messages.missing-primary-lock", "&c[更多用户] 牌子必须依附在已有锁牌的方块上。"));
        this.ownerOnlyMoreUsersMessage = color(config.getString("messages.owner-only-more-users", "&c只有所有者才能添加额外授权牌。"));
        this.extraUsersAttachedMessage = color(config.getString("messages.extra-users-attached", "&a已添加额外授权牌。"));
        this.reloadSuccessMessage = color(config.getString("messages.reload-success", "&aSignLock 配置已重载。"));
        this.reloadUsageMessage = color(config.getString("messages.reload-usage", "&e用法: /signlock reload"));
        this.noPermissionMessage = color(config.getString("messages.no-permission", "&c你没有权限执行此操作。"));
        this.addUsageMessage = color(config.getString("messages.add-usage", "&e用法: /bl add <玩家名>"));
        this.removeUsageMessage = color(config.getString("messages.remove-usage", "&e用法: /bl remove <玩家名>"));
        this.infoUsageMessage = color(config.getString("messages.info-usage", "&e用法: /bl info"));
        this.addSuccessMessage = color(config.getString("messages.add-success", "&a已将 &f%player% &a加入这个锁。"));
        this.addAlreadyAuthorizedMessage = color(config.getString("messages.add-already-authorized", "&e%player% 已经拥有这个锁的权限。"));
        this.addListFullMessage = color(config.getString("messages.add-list-full", "&c授权牌已满，且无法继续生成新的扩展牌。"));
        this.addInvalidSignMessage = color(config.getString("messages.add-invalid-sign", "&c请把准星对准一个有效的锁牌。"));
        this.addOnlyOwnerMessage = color(config.getString("messages.add-only-owner", "&c只有所有者才能给这个锁添加玩家。"));
        this.addPlayerNotFoundMessage = color(config.getString("messages.add-player-not-found", "&c请输入要添加的玩家名。"));
        this.removeSuccessMessage = color(config.getString("messages.remove-success", "&a已从这个锁中移除 &f%player%&a。"));
        this.removeNotFoundMessage = color(config.getString("messages.remove-not-found", "&e这个锁里没有玩家 &f%player%&e。"));
        this.removeOwnerDeniedMessage = color(config.getString("messages.remove-owner-denied", "&c不能移除锁的所有者。"));
        this.directLockNoSpaceMessage = color(config.getString("messages.direct-lock-no-space", "&c附近没有可放置锁牌的位置，请腾出一个侧面空间。"));
        this.infoHeaderMessage = color(config.getString("messages.info-header", "&6[SignLock] 锁信息"));
        this.infoOwnerMessage = color(config.getString("messages.info-owner", "&e所有者: &f%owner%"));
        this.infoPlayersMessage = color(config.getString("messages.info-players", "&e授权玩家: &f%players%"));
        this.infoNoPlayersMessage = color(config.getString("messages.info-no-players", "&e授权玩家: &f无"));
        this.infoExtensionsMessage = color(config.getString("messages.info-extensions", "&e扩展牌数量: &f%count%"));
        this.automationBlockedMessage = color(config.getString("messages.automation-blocked", "&c自动化装置无法操作已上锁容器。"));
        this.signEditDeniedMessage = color(config.getString("messages.sign-edit-denied", "&c你不能修改别人的锁牌内容。"));
        this.extensionCreatedMessage = color(config.getString("messages.extension-created", "&a已自动生成新的扩展牌。"));
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

    public String infoExtensionsMessage(int count) {
        return infoExtensionsMessage.replace("%count%", Integer.toString(count));
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

    private static String normalizeHeader(String value) {
        String stripped = ChatColor.stripColor(value);
        return stripped == null ? "" : stripped.trim();
    }

    private static String color(String value) {
        return ChatColor.translateAlternateColorCodes('&', value);
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
                ? List.of("NORTH", "SOUTH", "EAST", "WEST")
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
            faces.addAll(List.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST));
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
