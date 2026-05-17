package ym.signLock.listener;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ym.signLock.gui.LockManagementGuiService;
import ym.signLock.config.SignLockConfig;
import ym.signLock.service.LockService;
import ym.signLock.service.LockService.LockInfo;
import ym.signLock.service.LockService.LockType;
import ym.signLock.service.PlayerIdentityService;

import java.util.function.Consumer;

public final class LockListener implements Listener {

    private final LockService lockService;
    private final PlayerIdentityService playerIdentityService;
    private final LockManagementGuiService guiService;
    private final Consumer<Runnable> nextTick;
    private SignLockConfig config;

    public LockListener(LockService lockService, PlayerIdentityService playerIdentityService, SignLockConfig config) {
        this(lockService, playerIdentityService, config, null, Runnable::run);
    }

    public LockListener(
            LockService lockService,
            PlayerIdentityService playerIdentityService,
            SignLockConfig config,
            LockManagementGuiService guiService
    ) {
        this(lockService, playerIdentityService, config, guiService, Runnable::run);
    }

    public LockListener(
            LockService lockService,
            PlayerIdentityService playerIdentityService,
            SignLockConfig config,
            LockManagementGuiService guiService,
            Consumer<Runnable> nextTick
    ) {
        this.lockService = lockService;
        this.playerIdentityService = playerIdentityService;
        this.config = config;
        this.guiService = guiService;
        this.nextTick = nextTick;
    }

    public void setConfig(SignLockConfig config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        LockInfo managedLock = lockService.findManagedSignLock(event.getBlock());
        if (managedLock != null) {
            if (!lockService.canManage(managedLock, event.getPlayer())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(config.signEditDeniedMessage());
                return;
            }
            preserveManagedSignStructure(event, managedLock);
            return;
        }

        String rawHeader = cleanLine(event.getLine(0));
        if (rawHeader == null) {
            handleAutomaticLockSignPlacement(event);
            return;
        }

        if (matches(rawHeader, config.lockHeader())) {
            handleLockSignPlacement(event);
            return;
        }

        if (matches(rawHeader, config.moreUsersHeader())) {
            handleMoreUsersPlacement(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }

        Player player = event.getPlayer();
        if (tryDirectLock(event, player, clicked)) {
            return;
        }

        if (tryOpenManagedSignSurface(event, player, clicked)) {
            return;
        }

        LockInfo lock = lockService.findLock(clicked);
        if (lock == null || lockService.canAccess(lock, player)) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(config.lockedUseMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        Block target = lockService.resolveInventoryBlock(event.getInventory());
        if (target == null) {
            return;
        }

        LockInfo lock = lockService.findLock(target);
        if (lock == null || lockService.canAccess(lock, player)) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(config.lockedContainerMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        Block source = lockService.resolveInventoryBlock(event.getSource());
        Block destination = lockService.resolveInventoryBlock(event.getDestination());
        if (lockService.shouldBlockAutomationMove(source, destination)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        LockInfo directLock = lockService.findLock(block);
        if (directLock != null && !lockService.canBreak(directLock, player)) {
            event.setCancelled(true);
            player.sendMessage(config.protectedBlockMessage());
            return;
        }

        LockInfo signLock = lockService.findManagedSignLock(block);
        if (signLock != null && !lockService.canBreak(signLock, player)) {
            event.setCancelled(true);
            player.sendMessage(config.protectedSignMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (config.protectExplosions()) {
            event.blockList().removeIf(lockService::isExplosionProtected);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (config.protectExplosions()) {
            event.blockList().removeIf(lockService::isExplosionProtected);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (lockService.containsProtectedStructure(event.getBlocks())
                || lockService.wouldMoveIntoProtectedStructure(event.getBlocks(), event.getDirection())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (lockService.containsProtectedStructure(event.getBlocks())
                || lockService.wouldMoveIntoProtectedStructure(event.getBlocks(), event.getDirection())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFluidFlow(BlockFromToEvent event) {
        if (lockService.isProtectedStructure(event.getBlock()) || lockService.isProtectedStructure(event.getToBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (lockService.isProtectedStructure(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (lockService.isProtectedStructure(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    private void handleLockSignPlacement(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block target = lockService.findPlacementTarget(event.getBlock());
        if (target == null) {
            event.setCancelled(true);
            player.sendMessage(config.invalidLockPlacementMessage());
            return;
        }

        LockInfo existingLock = lockService.findLock(target);
        if (existingLock != null) {
            if (lockService.canManage(existingLock, player)) {
                handleMoreUsersPlacement(event);
                return;
            }
            event.setCancelled(true);
            player.sendMessage(config.alreadyProtectedMessage());
            return;
        }

        event.setLine(0, config.lockHeader());
        event.setLine(1, player.getName());
        event.setLine(2, normalizePlayerLine(event.getLine(2), player.getName()));
        event.setLine(3, normalizePlayerLine(event.getLine(3), player.getName()));
        playerIdentityService.remember(player);

        player.sendMessage(config.lockCreatedMessage(describe(target.getType())));
        player.sendMessage(config.lockUsageHintMessage());
    }

    private void handleAutomaticLockSignPlacement(SignChangeEvent event) {
        Block target = lockService.findPlacementTarget(event.getBlock());
        if (target == null) {
            return;
        }

        handleLockSignPlacement(event);
    }

    private void handleMoreUsersPlacement(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block target = lockService.findPlacementTarget(event.getBlock());
        if (target == null) {
            event.setCancelled(true);
            player.sendMessage(config.invalidMoreUsersPlacementMessage());
            return;
        }

        LockInfo lock = lockService.findLock(target);
        if (lock == null || lock.type() != LockType.PRIMARY) {
            event.setCancelled(true);
            player.sendMessage(config.missingPrimaryLockMessage());
            return;
        }

        if (!lockService.canManage(lock, player)) {
            event.setCancelled(true);
            player.sendMessage(config.ownerOnlyMoreUsersMessage());
            return;
        }

        if (!lockService.canCreateMoreUsersSign(target)) {
            event.setCancelled(true);
            player.sendMessage(config.addListFullMessage());
            return;
        }

        event.setLine(0, config.moreUsersHeader());
        event.setLine(1, "");
        event.setLine(2, "");
        event.setLine(3, "");
        player.sendMessage(config.extraUsersAttachedMessage());
    }

    private boolean tryDirectLock(PlayerInteractEvent event, Player player, Block clicked) {
        if (player.isSneaking()) {
            return false;
        }

        ItemStack item = event.getItem();
        if (item == null || !isSignItem(item.getType())) {
            return false;
        }

        Block target = lockService.resolveDirectLockTarget(clicked);
        if (target == null) {
            return false;
        }

        if (lockService.findLock(target) != null) {
            return false;
        }

        BlockFace clickedFace = event.getBlockFace();
        if (!lockService.createLockSign(target, clickedFace, item.getType(), player)) {
            player.sendMessage(config.directLockNoSpaceMessage());
            event.setCancelled(true);
            return true;
        }

        consumeOneSign(player, item);
        playerIdentityService.remember(player);
        player.sendMessage(config.lockCreatedMessage(describe(target.getType())));
        player.sendMessage(config.lockUsageHintMessage());
        event.setCancelled(true);
        return true;
    }

    private boolean tryOpenManagedSignSurface(PlayerInteractEvent event, Player player, Block clicked) {
        if (!(clicked.getState() instanceof Sign sign)) {
            return false;
        }

        if (!lockService.isManagedHeaderLine(sign.getLine(0))) {
            return false;
        }

        event.setCancelled(true);
        event.setUseInteractedBlock(Result.DENY);
        event.setUseItemInHand(Result.DENY);

        LockInfo lock = lockService.findManagedSignLock(clicked);
        if (lock == null) {
            return true;
        }

        if (guiService == null) {
            return true;
        }

        if (!lockService.canAccess(lock, player)) {
            player.sendMessage(config.protectedSignMessage());
            return true;
        }

        nextTick.accept(() -> guiService.openFor(player, sign));
        return true;
    }

    private void preserveManagedSignStructure(SignChangeEvent event, LockInfo lock) {
        if (isExtraUsersSign(event.getBlock())) {
            event.setLine(0, config.moreUsersHeader());
            return;
        }

        if (lock.type() == LockType.PRIMARY) {
            event.setLine(0, config.lockHeader());
            event.setLine(1, lock.owner());
        } else {
            event.setLine(0, config.moreUsersHeader());
        }
    }

    private boolean isExtraUsersSign(Block block) {
        if (!(block.getState() instanceof Sign sign)) {
            return false;
        }

        String header = cleanLine(sign.getLine(0));
        return header != null && matches(header, config.moreUsersHeader());
    }

    private static String normalizePlayerLine(String value, String owner) {
        String cleaned = cleanLine(value);
        if (cleaned == null || cleaned.equalsIgnoreCase(owner)) {
            return "";
        }
        return cleaned;
    }

    private static boolean matches(String rawHeader, String expected) {
        return rawHeader.equalsIgnoreCase(expected);
    }

    private static String cleanLine(String line) {
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

    private static String describe(Material material) {
        return material.name().toLowerCase().replace('_', ' ');
    }

    private static boolean isSignItem(Material material) {
        String name = material.name();
        return name.endsWith("_SIGN") || name.endsWith("_HANGING_SIGN");
    }

    private static void consumeOneSign(Player player, ItemStack item) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        int amount = item.getAmount();
        if (amount <= 1) {
            item.setAmount(0);
            return;
        }
        item.setAmount(amount - 1);
    }
}
