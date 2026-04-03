package ym.signLock.service;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import ym.signLock.config.SignLockConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class LockService {

    private static final Set<BlockFace> ADJACENT_FACES = EnumSet.of(
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.UP,
            BlockFace.DOWN
    );
    private static final List<BlockFace> HORIZONTAL_FACES = List.of(
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST
    );

    private SignLockConfig config;
    private final PlayerIdentityService playerIdentityService;

    public LockService(SignLockConfig config, PlayerIdentityService playerIdentityService) {
        this.config = config;
        this.playerIdentityService = playerIdentityService;
    }

    public void setConfig(SignLockConfig config) {
        this.config = config;
    }

    public Block findPlacementTarget(Block signBlock) {
        Block attachedCandidate = resolveAttachedBlock(signBlock);
        if (attachedCandidate != null && isLockable(attachedCandidate)) {
            return canonicalTarget(attachedCandidate);
        }

        for (BlockFace face : ADJACENT_FACES) {
            Block relative = signBlock.getRelative(face);
            if (isLockable(relative)) {
                return canonicalTarget(relative);
            }
        }

        return null;
    }

    public Block resolveDirectLockTarget(Block clickedBlock) {
        return canonicalTarget(clickedBlock);
    }

    public LockInfo findLock(Block block) {
        return findPrimaryLock(canonicalTarget(block));
    }

    public LockInfo findManagedSignLock(Block block) {
        if (!(block.getState() instanceof Sign sign)) {
            return null;
        }

        String header = normalizeLine(sign.getLine(0));
        if (header == null) {
            return null;
        }

        if (!header.equalsIgnoreCase(config.lockHeader()) && !header.equalsIgnoreCase(config.moreUsersHeader())) {
            return null;
        }

        Block target = findPlacementTarget(block);
        return target == null ? null : findLock(target);
    }

    public LockDetails describeLock(Block signBlock) {
        LockInfo lock = findManagedSignLock(signBlock);
        if (lock == null) {
            return null;
        }
        List<String> players = new ArrayList<>(lock.allowedPlayers());
        return new LockDetails(lock.owner(), players, usedExtensionCount(players.size()), describeTarget(lock.targetBlock()));
    }

    public LockViewerScope viewerScope(LockInfo lock, Player player) {
        if (canManage(lock, player)) {
            return LockViewerScope.MANAGE;
        }
        if (canAccess(lock, player)) {
            return LockViewerScope.ACCESS;
        }
        return LockViewerScope.DENIED;
    }

    public boolean canAccess(LockInfo lock, Player player) {
        if (config.adminBypass() && player.hasPermission("signlock.admin")) {
            return true;
        }

        if (matchesIdentity(lock.owner(), player)) {
            return true;
        }

        for (String allowed : lock.allowedPlayers()) {
            if (matchesIdentity(allowed, player)) {
                return true;
            }
        }
        return false;
    }

    public boolean canManage(LockInfo lock, Player player) {
        return (config.adminBypass() && player.hasPermission("signlock.admin"))
                || matchesIdentity(lock.owner(), player);
    }

    public boolean canBreak(LockInfo lock, Player player) {
        return canManage(lock, player);
    }

    public boolean isProtectedStructure(Block block) {
        return findLock(block) != null || findManagedSignLock(block) != null;
    }

    public boolean isExplosionProtected(Block block) {
        return isProtectedStructure(block);
    }

    public boolean isManagedLockSign(Block block) {
        return findManagedSignLock(block) != null;
    }

    public boolean isProtectedAutomationTarget(Block block) {
        return isProtectedStructure(block);
    }

    public boolean containsProtectedStructure(Iterable<Block> blocks) {
        for (Block block : blocks) {
            if (isProtectedStructure(block)) {
                return true;
            }
        }
        return false;
    }

    public boolean wouldMoveIntoProtectedStructure(Iterable<Block> blocks, BlockFace direction) {
        if (direction == null) {
            return false;
        }
        for (Block block : blocks) {
            if (isProtectedStructure(block.getRelative(direction))) {
                return true;
            }
        }
        return false;
    }

    public boolean isAuthorizedOnLock(LockInfo lock, String playerName) {
        if (sameIdentity(lock.owner(), playerName)) {
            return true;
        }

        for (String allowed : lock.allowedPlayers()) {
            if (sameIdentity(allowed, playerName)) {
                return true;
            }
        }
        return false;
    }

    public AddPlayerResult addPlayerToLock(Sign preferredSign, LockInfo lock, String playerName) {
        if (isAuthorizedOnLock(lock, playerName)) {
            return AddPlayerResult.ALREADY_AUTHORIZED;
        }

        for (Sign sign : collectManagedSigns(lock.targetBlock(), preferredSign)) {
            if (tryWritePlayer(sign, playerName)) {
                return AddPlayerResult.ADDED;
            }
        }

        Sign created = createMoreUsersSign(lock.targetBlock(), preferredSign.getBlock().getType());
        if (created == null) {
            return AddPlayerResult.NO_SPACE;
        }

        return tryWritePlayer(created, playerName) ? AddPlayerResult.ADDED_WITH_EXTENSION : AddPlayerResult.NO_SPACE;
    }

    public RemovePlayerResult removePlayerFromLock(Sign preferredSign, LockInfo lock, String playerName) {
        if (sameIdentity(lock.owner(), playerName)) {
            return RemovePlayerResult.OWNER_DENIED;
        }

        for (Sign sign : collectManagedSigns(lock.targetBlock(), preferredSign)) {
            if (removePlayerFromSign(sign, playerName)) {
                cleanupEmptyExtension(sign);
                return RemovePlayerResult.REMOVED;
            }
        }
        return RemovePlayerResult.NOT_FOUND;
    }

    public boolean createLockSign(Block target, BlockFace clickedFace, Material signItem, Player owner) {
        Block normalizedTarget = normalizeProtectedBlock(target);
        if (normalizedTarget == null || findLock(normalizedTarget) != null) {
            return false;
        }

        Material signBlockMaterial = toWallSignMaterial(signItem);
        if (signBlockMaterial == null) {
            return false;
        }

        for (BlockFace face : preferredPlacementFaces(clickedFace, true)) {
            Sign placed = createSignOnFace(normalizedTarget, face, signBlockMaterial);
            if (placed == null) {
                continue;
            }

            placed.setLine(0, config.lockHeader());
            placed.setLine(1, owner.getName());
            placed.setLine(2, "");
            placed.setLine(3, "");
            placed.update(true, false);
            return true;
        }

        return false;
    }

    public Sign createMoreUsersSign(Block target, Material signMaterial) {
        Block normalizedTarget = normalizeProtectedBlock(target);
        if (normalizedTarget == null) {
            return null;
        }

        if (!canCreateMoreUsersSign(normalizedTarget)) {
            return null;
        }

        for (Block related : collectRelatedBlocks(normalizedTarget)) {
            for (BlockFace face : preferredPlacementFaces(null, false)) {
                Sign placed = createSignOnFace(related, face, signMaterial);
                if (placed == null) {
                    continue;
                }

                placed.setLine(0, config.moreUsersHeader());
                placed.setLine(1, "");
                placed.setLine(2, "");
                placed.setLine(3, "");
                placed.update(true, false);
                return placed;
            }
        }

        return null;
    }

    public boolean canCreateMoreUsersSign(Block target) {
        Block normalizedTarget = normalizeProtectedBlock(target);
        if (normalizedTarget == null) {
            return false;
        }
        return countMoreUserSigns(normalizedTarget) < maxMoreUserSigns(normalizedTarget);
    }

    public Block resolveInventoryBlock(InventoryHolder holder) {
        if (holder instanceof Container container) {
            return canonicalTarget(container.getBlock());
        }

        if (holder instanceof DoubleChest doubleChest) {
            Location location = doubleChest.getLocation();
            return location == null ? null : canonicalTarget(location.getBlock());
        }

        return null;
    }

    private LockInfo findPrimaryLock(Block normalizedTarget) {
        if (normalizedTarget == null || !isLockable(normalizedTarget)) {
            return null;
        }

        for (Block related : collectRelatedBlocks(normalizedTarget)) {
            for (BlockFace face : ADJACENT_FACES) {
                Block signBlock = related.getRelative(face);
                if (!(signBlock.getState() instanceof Sign sign)) {
                    continue;
                }

                String header = normalizeLine(sign.getLine(0));
                if (header == null || !header.equalsIgnoreCase(config.lockHeader())) {
                    continue;
                }

                Block attached = canonicalTarget(resolveAttachedBlock(signBlock));
                if (!isSameLockTarget(normalizedTarget, attached)) {
                    continue;
                }

                String owner = normalizeLine(sign.getLine(1));
                if (owner == null) {
                    continue;
                }

                Set<String> allowedPlayers = new LinkedHashSet<>(readAllowedPlayers(sign, true));
                allowedPlayers.addAll(readExtraAllowedPlayers(normalizedTarget));
                return new LockInfo(signBlock, normalizedTarget, owner, allowedPlayers, LockType.PRIMARY);
            }
        }
        return null;
    }

    private Set<String> readExtraAllowedPlayers(Block normalizedTarget) {
        Set<String> allowedPlayers = new LinkedHashSet<>();
        for (Block related : collectRelatedBlocks(normalizedTarget)) {
            for (BlockFace face : ADJACENT_FACES) {
                Block signBlock = related.getRelative(face);
                if (!(signBlock.getState() instanceof Sign sign)) {
                    continue;
                }

                String header = normalizeLine(sign.getLine(0));
                if (header == null || !header.equalsIgnoreCase(config.moreUsersHeader())) {
                    continue;
                }

                Block attached = canonicalTarget(resolveAttachedBlock(signBlock));
                if (!isSameLockTarget(normalizedTarget, attached)) {
                    continue;
                }

                allowedPlayers.addAll(readAllowedPlayers(sign, false));
            }
        }
        return allowedPlayers;
    }

    private Set<String> readAllowedPlayers(Sign sign, boolean primary) {
        Set<String> allowedPlayers = new LinkedHashSet<>();
        int startIndex = primary ? 2 : 1;
        for (int index = startIndex; index < 4; index++) {
            String line = normalizeLine(sign.getLine(index));
            if (line != null) {
                allowedPlayers.add(line);
            }
        }
        return allowedPlayers;
    }

    private List<Block> collectRelatedBlocks(Block block) {
        ProtectedTarget target = resolveProtectedTarget(block);
        if (target == null) {
            return List.of();
        }
        return target.relatedBlocks();
    }

    private Block normalizeProtectedBlock(Block block) {
        ProtectedTarget target = resolveProtectedTarget(block);
        return target == null ? null : target.canonicalBlock();
    }

    private Block canonicalTarget(Block block) {
        ProtectedTarget target = resolveProtectedTarget(block);
        return target == null ? null : target.canonicalBlock();
    }

    private ProtectedTarget resolveProtectedTarget(Block block) {
        if (block == null || !isLockable(block)) {
            return null;
        }

        Block normalized = normalizeBaseBlock(block);
        if (normalized == null || !isLockable(normalized)) {
            return null;
        }

        LinkedHashSet<Block> related = new LinkedHashSet<>();
        related.add(normalized);

        BlockData data = normalized.getBlockData();
        if (data instanceof Chest chest && chest.getType() != Chest.Type.SINGLE) {
            Block otherHalf = normalizeBaseBlock(findAdjacentChestHalf(normalized, chest.getFacing()));
            if (otherHalf != null) {
                related.add(otherHalf);
            }
        }

        if (data instanceof Door door && door.getHalf() == Bisected.Half.BOTTOM) {
            Block top = normalized.getRelative(BlockFace.UP);
            if (top.getBlockData() instanceof Door) {
                related.add(top);
            }
        }

        List<Block> relatedBlocks = List.copyOf(related);
        return new ProtectedTarget(selectCanonicalBlock(relatedBlocks), relatedBlocks);
    }

    private Block normalizeBaseBlock(Block block) {
        if (block == null || !isLockable(block)) {
            return null;
        }

        BlockData data = block.getBlockData();
        if (data instanceof Door door && door.getHalf() == Bisected.Half.TOP) {
            return block.getRelative(BlockFace.DOWN);
        }

        return block;
    }

    private Block selectCanonicalBlock(List<Block> blocks) {
        return blocks.stream()
                .min(Comparator
                        .comparing((Block candidate) -> candidate.getWorld().getUID())
                        .thenComparingInt(Block::getX)
                        .thenComparingInt(Block::getY)
                        .thenComparingInt(Block::getZ))
                .orElseThrow();
    }

    private Block resolveAttachedBlock(Block signBlock) {
        BlockData data = signBlock.getBlockData();
        if (data instanceof Directional directional) {
            return signBlock.getRelative(directional.getFacing().getOppositeFace());
        }

        for (BlockFace face : ADJACENT_FACES) {
            Block relative = signBlock.getRelative(face);
            if (isLockable(relative)) {
                return relative;
            }
        }
        return null;
    }

    private Block findAdjacentChestHalf(Block chestBlock, BlockFace facing) {
        for (BlockFace face : HORIZONTAL_FACES) {
            Block relative = chestBlock.getRelative(face);
            if (!(relative.getBlockData() instanceof Chest otherChest)) {
                continue;
            }

            if (otherChest.getType() == Chest.Type.SINGLE || otherChest.getFacing() != facing) {
                continue;
            }

            return relative;
        }
        return null;
    }

    private boolean isLockable(Block block) {
        return block != null && config.isLockable(block.getType());
    }

    private List<Sign> collectManagedSigns(Block target, Sign preferredSign) {
        List<Sign> result = new ArrayList<>();
        Block normalizedTarget = normalizeProtectedBlock(target);
        if (normalizedTarget == null) {
            return result;
        }

        if (preferredSign != null && isManagedSignForTarget(preferredSign, normalizedTarget)) {
            result.add(preferredSign);
        }

        for (Block related : collectRelatedBlocks(normalizedTarget)) {
            for (BlockFace face : ADJACENT_FACES) {
                Block signBlock = related.getRelative(face);
                if (!(signBlock.getState() instanceof Sign sign)) {
                    continue;
                }
                if (!isManagedSignForTarget(sign, normalizedTarget) || containsSign(result, sign)) {
                    continue;
                }
                result.add(sign);
            }
        }
        return result;
    }

    private boolean isManagedSignForTarget(Sign sign, Block normalizedTarget) {
        String header = normalizeLine(sign.getLine(0));
        if (header == null) {
            return false;
        }
        if (!header.equalsIgnoreCase(config.lockHeader()) && !header.equalsIgnoreCase(config.moreUsersHeader())) {
            return false;
        }

        Block attached = findPlacementTarget(sign.getBlock());
        return isSameLockTarget(attached, normalizedTarget);
    }

    private boolean containsSign(List<Sign> signs, Sign target) {
        for (Sign sign : signs) {
            if (sameBlock(sign.getBlock(), target.getBlock())) {
                return true;
            }
        }
        return false;
    }

    private boolean tryWritePlayer(Sign sign, String playerName) {
        String header = normalizeLine(sign.getLine(0));
        if (header == null) {
            return false;
        }

        int startIndex = header.equalsIgnoreCase(config.lockHeader()) ? 2 : 1;
        for (int index = startIndex; index < 4; index++) {
            String line = normalizeLine(sign.getLine(index));
            if (line != null && sameIdentity(line, playerName)) {
                return true;
            }
        }

        for (int index = startIndex; index < 4; index++) {
            String line = normalizeLine(sign.getLine(index));
            if (line == null) {
                sign.setLine(index, playerName);
                sign.update(true, false);
                return true;
            }
        }
        return false;
    }

    private boolean removePlayerFromSign(Sign sign, String playerName) {
        String header = normalizeLine(sign.getLine(0));
        if (header == null) {
            return false;
        }

        int startIndex = header.equalsIgnoreCase(config.lockHeader()) ? 2 : 1;
        for (int index = startIndex; index < 4; index++) {
            String line = normalizeLine(sign.getLine(index));
            if (line != null && sameIdentity(line, playerName)) {
                sign.setLine(index, "");
                sign.update(true, false);
                return true;
            }
        }
        return false;
    }

    private void cleanupEmptyExtension(Sign sign) {
        String header = normalizeLine(sign.getLine(0));
        if (header == null || !header.equalsIgnoreCase(config.moreUsersHeader())) {
            return;
        }

        for (int index = 1; index < 4; index++) {
            if (normalizeLine(sign.getLine(index)) != null) {
                return;
            }
        }

        sign.getBlock().setType(Material.AIR, false);
    }

    private int countMoreUserSigns(Block target) {
        int count = 0;
        for (Sign sign : collectManagedSigns(target, null)) {
            String header = normalizeLine(sign.getLine(0));
            if (header != null && header.equalsIgnoreCase(config.moreUsersHeader())) {
                count++;
            }
        }
        return count;
    }

    private int usedExtensionCount(int allowedPlayerCount) {
        int overflow = Math.max(0, allowedPlayerCount - 2);
        return overflow == 0 ? 0 : (overflow + 2) / 3;
    }

    private int maxMoreUserSigns(Block target) {
        Block normalizedTarget = normalizeProtectedBlock(target);
        if (normalizedTarget == null) {
            return 0;
        }
        return config.maxMoreUserSigns();
    }

    private LockTargetDetails describeTarget(Block targetBlock) {
        Block normalizedTarget = normalizeProtectedBlock(targetBlock);
        if (normalizedTarget == null) {
            return null;
        }

        LockTargetKind kind = LockTargetKind.GENERIC;
        BlockData data = normalizedTarget.getBlockData();
        if (data instanceof Chest chest) {
            kind = chest.getType() == Chest.Type.SINGLE ? LockTargetKind.SINGLE_CHEST : LockTargetKind.DOUBLE_CHEST;
        }

        return new LockTargetDetails(
                normalizedTarget.getType().name(),
                normalizedTarget.getWorld().getName(),
                normalizedTarget.getX(),
                normalizedTarget.getY(),
                normalizedTarget.getZ(),
                kind
        );
    }

    private List<BlockFace> preferredPlacementFaces(BlockFace clickedFace, boolean prioritizeClickFace) {
        List<BlockFace> faces = new ArrayList<>();
        if (prioritizeClickFace && clickedFace != null && HORIZONTAL_FACES.contains(clickedFace)) {
            faces.add(clickedFace);
        }
        for (BlockFace face : config.extensionPlacementOrder()) {
            if (HORIZONTAL_FACES.contains(face) && !faces.contains(face)) {
                faces.add(face);
            }
        }
        for (BlockFace face : HORIZONTAL_FACES) {
            if (!faces.contains(face)) {
                faces.add(face);
            }
        }
        return faces;
    }

    private Sign createSignOnFace(Block target, BlockFace face, Material signMaterial) {
        Block signBlock = target.getRelative(face);
        if (!signBlock.getType().isAir()) {
            return null;
        }

        signBlock.setType(signMaterial, false);
        BlockData data = signBlock.getBlockData();
        if (!(data instanceof Directional directional)) {
            signBlock.setType(Material.AIR, false);
            return null;
        }

        directional.setFacing(face);
        signBlock.setBlockData(data, false);
        if (!(signBlock.getState() instanceof Sign signState)) {
            signBlock.setType(Material.AIR, false);
            return null;
        }
        return signState;
    }

    private Material toWallSignMaterial(Material signItem) {
        String name = signItem.name();
        try {
            if (name.endsWith("_HANGING_SIGN")) {
                return Material.valueOf(name.replace("_HANGING_SIGN", "_WALL_HANGING_SIGN"));
            }
            if (name.endsWith("_SIGN")) {
                return Material.valueOf(name.replace("_SIGN", "_WALL_SIGN"));
            }
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }

    private static String normalizeLine(String line) {
        if (line == null) {
            return null;
        }

        String stripped = ChatColor.stripColor(line);
        if (stripped == null) {
            return null;
        }

        String normalized = stripped.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean sameBlock(Block first, Block second) {
        return first.getWorld().equals(second.getWorld())
                && first.getX() == second.getX()
                && first.getY() == second.getY()
                && first.getZ() == second.getZ();
    }

    private boolean isSameLockTarget(Block first, Block second) {
        Block normalizedFirst = normalizeProtectedBlock(first);
        Block normalizedSecond = normalizeProtectedBlock(second);
        if (normalizedFirst == null || normalizedSecond == null) {
            return false;
        }
        return sameBlock(normalizedFirst, normalizedSecond);
    }

    private boolean matchesIdentity(String storedName, Player player) {
        if (storedName.equalsIgnoreCase(player.getName())) {
            return true;
        }

        UUID storedUuid = playerIdentityService.findUuidByName(storedName);
        return storedUuid != null && storedUuid.equals(player.getUniqueId());
    }

    private boolean sameIdentity(String firstName, String secondName) {
        if (firstName.equalsIgnoreCase(secondName)) {
            return true;
        }

        UUID firstUuid = playerIdentityService.findUuidByName(firstName);
        UUID secondUuid = playerIdentityService.findUuidByName(secondName);
        return firstUuid != null && firstUuid.equals(secondUuid);
    }

    public enum LockType {
        PRIMARY,
        EXTRA_USERS
    }

    public enum AddPlayerResult {
        ADDED,
        ADDED_WITH_EXTENSION,
        ALREADY_AUTHORIZED,
        NO_SPACE
    }

    public enum RemovePlayerResult {
        REMOVED,
        NOT_FOUND,
        OWNER_DENIED
    }

    public enum LockViewerScope {
        MANAGE,
        ACCESS,
        DENIED;

        public boolean readOnly() {
            return this != MANAGE;
        }

        public boolean canViewAuthorizedPlayers() {
            return this != DENIED;
        }
    }

    public enum LockTargetKind {
        SINGLE_CHEST,
        DOUBLE_CHEST,
        GENERIC
    }

    public record LockInfo(Block signBlock, Block targetBlock, String owner, Set<String> allowedPlayers, LockType type) {
    }

    public record LockDetails(String owner, List<String> allowedPlayers, int extensionCount, LockTargetDetails target) {

        public LockDetails {
            allowedPlayers = List.copyOf(allowedPlayers);
        }

        public LockDetails(String owner, List<String> allowedPlayers, int extensionCount) {
            this(owner, allowedPlayers, extensionCount, null);
        }
    }

    public record LockTargetDetails(String blockType, String worldName, int x, int y, int z, LockTargetKind kind) {
    }

    private record ProtectedTarget(Block canonicalBlock, List<Block> relatedBlocks) {
    }
}
