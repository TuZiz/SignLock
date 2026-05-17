package ym.signLock.gui;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ym.signLock.config.LockGuiConfig;
import ym.signLock.config.SignLockConfig;
import ym.signLock.service.LockService;

public final class LockManagementGuiService {

    private final LockService lockService;
    private SignLockConfig config;
    private LockGuiConfig guiConfig;

    public LockManagementGuiService(LockService lockService, SignLockConfig config) {
        this(lockService, config, new LockGuiConfig(new org.bukkit.configuration.file.YamlConfiguration()));
    }

    public LockManagementGuiService(LockService lockService, SignLockConfig config, LockGuiConfig guiConfig) {
        this.lockService = lockService;
        this.config = config;
        this.guiConfig = guiConfig;
    }

    public void setConfig(SignLockConfig config) {
        this.config = config;
    }

    public void setGuiConfig(LockGuiConfig guiConfig) {
        this.guiConfig = guiConfig;
    }

    public boolean openFor(Player player, Sign sign) {
        return openFor(player, sign.getBlock());
    }

    public boolean openFor(Player player, LockManagementGuiHolder holder) {
        player.openInventory(buildInventory(holder));
        return true;
    }

    public boolean openFor(Player player, LockManagementSession session) {
        Block signBlock = session.resolveSignBlock();
        return signBlock != null && openFor(player, signBlock);
    }

    public boolean openFor(Player player, Block signBlock) {
        LockManagementGuiHolder holder = createHolder(player, signBlock);
        if (holder == null) {
            player.sendMessage(config.addInvalidSignMessage());
            return false;
        }

        player.openInventory(buildInventory(holder));
        return true;
    }

    LockManagementGuiHolder createHolder(Block signBlock) {
        LockService.LockInfo lock = lockService.findManagedSignLock(signBlock);
        LockService.LockDetails details = lockService.describeLock(signBlock);
        if (lock == null || details == null) {
            return null;
        }
        return new LockManagementGuiHolder(
                LockManagementSession.from(lock),
                LockSummaryView.from(lock, details, LockService.LockViewerScope.MANAGE),
                guiConfig
        );
    }

    LockManagementGuiHolder createHolder(Player player, Block signBlock) {
        LockService.LockInfo lock = lockService.findManagedSignLock(signBlock);
        LockService.LockDetails details = lockService.describeLock(signBlock);
        if (lock == null || details == null) {
            return null;
        }
        LockService.LockViewerScope viewerScope = lockService.viewerScope(lock, player);
        if (viewerScope == LockService.LockViewerScope.DENIED) {
            return null;
        }
        return new LockManagementGuiHolder(LockManagementSession.from(lock), LockSummaryView.from(lock, details, viewerScope), guiConfig);
    }

    Inventory buildInventory(LockManagementGuiHolder holder) {
        return LockManagementGui.createInventory(holder, config, guiConfig);
    }
}
