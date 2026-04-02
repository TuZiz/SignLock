package ym.signLock;

import org.bukkit.plugin.java.JavaPlugin;
import ym.signLock.command.SignLockCommand;
import ym.signLock.config.SignLockConfig;
import ym.signLock.listener.LockListener;
import ym.signLock.listener.PlayerIdentityListener;
import ym.signLock.service.LockService;
import ym.signLock.service.PlayerIdentityService;

public final class SignLock extends JavaPlugin {

    private LockService lockService;
    private LockListener lockListener;
    private SignLockConfig signLockConfig;
    private PlayerIdentityService playerIdentityService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        signLockConfig = new SignLockConfig(getConfig());
        playerIdentityService = new PlayerIdentityService(this);
        playerIdentityService.preload(getServer().getOfflinePlayers());
        lockService = new LockService(signLockConfig, playerIdentityService);
        lockListener = new LockListener(lockService, playerIdentityService, signLockConfig);
        getServer().getPluginManager().registerEvents(lockListener, this);
        getServer().getPluginManager().registerEvents(new PlayerIdentityListener(playerIdentityService), this);
        SignLockCommand signLockCommand = new SignLockCommand(this);
        if (getCommand("signlock") != null) {
            getCommand("signlock").setExecutor(signLockCommand);
            getCommand("signlock").setTabCompleter(signLockCommand);
        }
        getLogger().info("SignLock 已启用，配置与玩家身份缓存已加载。");
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
