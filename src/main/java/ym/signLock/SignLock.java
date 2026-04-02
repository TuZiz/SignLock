package ym.signLock;

import org.bukkit.plugin.java.JavaPlugin;
import ym.signLock.command.SignLockCommand;
import ym.signLock.config.SignLockConfig;
import ym.signLock.gui.LockManagementGuiActionService;
import ym.signLock.gui.LockManagementGuiService;
import ym.signLock.gui.LockManagementPendingInputStore;
import ym.signLock.listener.LockGuiChatInputListener;
import ym.signLock.listener.LockGuiListener;
import ym.signLock.listener.LockListener;
import ym.signLock.listener.PlayerIdentityListener;
import ym.signLock.service.LockBatchAuthorizationService;
import ym.signLock.service.LockBatchTargetParser;
import ym.signLock.service.LockPlayerNameNormalizer;
import ym.signLock.service.LockService;
import ym.signLock.service.PlayerIdentityService;

import java.util.function.Consumer;

public final class SignLock extends JavaPlugin {

    private LockService lockService;
    private LockListener lockListener;
    private SignLockConfig signLockConfig;
    private PlayerIdentityService playerIdentityService;
    private LockManagementGuiService lockManagementGuiService;
    private LockManagementGuiActionService lockManagementGuiActionService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        signLockConfig = new SignLockConfig(getConfig());
        playerIdentityService = new PlayerIdentityService(this);
        playerIdentityService.preload(getServer().getOfflinePlayers());
        lockService = new LockService(signLockConfig, playerIdentityService);

        LockPlayerNameNormalizer playerNameNormalizer = new LockPlayerNameNormalizer(playerIdentityService);
        LockBatchTargetParser batchTargetParser = new LockBatchTargetParser();
        LockBatchAuthorizationService batchAuthorizationService = new LockBatchAuthorizationService(lockService, playerNameNormalizer);
        LockManagementPendingInputStore pendingInputStore = new LockManagementPendingInputStore();
        lockManagementGuiService = new LockManagementGuiService(lockService, signLockConfig);
        lockManagementGuiActionService = new LockManagementGuiActionService(
                lockService,
                batchTargetParser,
                batchAuthorizationService,
                playerNameNormalizer,
                lockManagementGuiService,
                pendingInputStore,
                signLockConfig
        );

        lockListener = new LockListener(lockService, playerIdentityService, signLockConfig, lockManagementGuiService);
        Consumer<Runnable> nextTick = task -> getServer().getScheduler().runTask(this, task);

        getServer().getPluginManager().registerEvents(lockListener, this);
        getServer().getPluginManager().registerEvents(new LockGuiListener(nextTick, lockManagementGuiActionService), this);
        getServer().getPluginManager().registerEvents(new LockGuiChatInputListener(nextTick, pendingInputStore, lockManagementGuiActionService), this);
        getServer().getPluginManager().registerEvents(new PlayerIdentityListener(playerIdentityService), this);

        SignLockCommand signLockCommand = new SignLockCommand(this);
        if (getCommand("signlock") != null) {
            getCommand("signlock").setExecutor(signLockCommand);
            getCommand("signlock").setTabCompleter(signLockCommand);
        }

        getLogger().info("SignLock 已启用，配置、锁服务和 GUI 管理入口已加载。");
    }

    @Override
    public void onDisable() {
        playerIdentityService.save();
        getLogger().info("SignLock 已关闭，玩家身份缓存已保存。");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        signLockConfig = new SignLockConfig(getConfig());
        lockService.setConfig(signLockConfig);
        lockListener.setConfig(signLockConfig);
        lockManagementGuiService.setConfig(signLockConfig);
        lockManagementGuiActionService.setConfig(signLockConfig);
        getLogger().info("SignLock 配置已重载。");
    }

    public SignLockConfig getSignLockConfig() {
        return signLockConfig;
    }

    public PlayerIdentityService getPlayerIdentityService() {
        return playerIdentityService;
    }

    public LockService getLockService() {
        return lockService;
    }
}
