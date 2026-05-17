package ym.signLock;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ym.signLock.command.SignLockCommand;
import ym.signLock.config.LockGuiConfig;
import ym.signLock.config.SignLockConfig;
import ym.signLock.gui.LockManagementGuiActionService;
import ym.signLock.gui.LockManagementGuiService;
import ym.signLock.gui.LockManagementPendingInputStore;
import ym.signLock.listener.LockGuiChatInputListener;
import ym.signLock.listener.LockGuiListener;
import ym.signLock.listener.LockListener;
import ym.signLock.listener.PaperAsyncChatBridgeRegistrar;
import ym.signLock.listener.PlayerIdentityListener;
import ym.signLock.platform.SignLockScheduler;
import ym.signLock.service.LockBatchAuthorizationService;
import ym.signLock.service.LockBatchTargetParser;
import ym.signLock.service.LockPlayerNameNormalizer;
import ym.signLock.service.LockService;
import ym.signLock.service.PlayerIdentityService;

import java.io.File;

public final class SignLock extends JavaPlugin {

    private LockService lockService;
    private LockListener lockListener;
    private SignLockConfig signLockConfig;
    private LockGuiConfig lockGuiConfig;
    private PlayerIdentityService playerIdentityService;
    private LockManagementGuiService lockManagementGuiService;
    private LockManagementGuiActionService lockManagementGuiActionService;
    private SignLockScheduler scheduler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveDefaultGuiConfig();

        signLockConfig = new SignLockConfig(getConfig());
        lockGuiConfig = loadGuiConfig();
        playerIdentityService = new PlayerIdentityService(this);
        scheduler = SignLockScheduler.create(this);
        lockService = new LockService(signLockConfig, playerIdentityService);

        LockPlayerNameNormalizer playerNameNormalizer = new LockPlayerNameNormalizer(playerIdentityService);
        LockBatchTargetParser batchTargetParser = new LockBatchTargetParser();
        LockBatchAuthorizationService batchAuthorizationService = new LockBatchAuthorizationService(lockService, playerNameNormalizer);
        LockManagementPendingInputStore pendingInputStore = new LockManagementPendingInputStore();
        lockManagementGuiService = new LockManagementGuiService(lockService, signLockConfig, lockGuiConfig);
        lockManagementGuiActionService = new LockManagementGuiActionService(
                lockService,
                batchTargetParser,
                batchAuthorizationService,
                playerNameNormalizer,
                lockManagementGuiService,
                pendingInputStore,
                signLockConfig,
                lockGuiConfig
        );

        lockListener = new LockListener(lockService, playerIdentityService, signLockConfig, lockManagementGuiService, scheduler);

        getServer().getPluginManager().registerEvents(lockListener, this);
        getServer().getPluginManager().registerEvents(new LockGuiListener(scheduler, lockManagementGuiActionService), this);
        LockGuiChatInputListener chatInputListener = new LockGuiChatInputListener(scheduler, pendingInputStore, lockManagementGuiActionService);
        getServer().getPluginManager().registerEvents(chatInputListener, this);
        PaperAsyncChatBridgeRegistrar.register(this, chatInputListener);
        getServer().getPluginManager().registerEvents(new PlayerIdentityListener(playerIdentityService), this);

        SignLockCommand signLockCommand = new SignLockCommand(this);
        if (getCommand("signlock") != null) {
            getCommand("signlock").setExecutor(signLockCommand);
            getCommand("signlock").setTabCompleter(signLockCommand);
        }

        getLogger().info("SignLock enabled with gui.yml support.");
    }

    @Override
    public void onDisable() {
        playerIdentityService.save();
        getLogger().info("SignLock disabled.");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        signLockConfig = new SignLockConfig(getConfig());
        lockGuiConfig = loadGuiConfig();
        lockService.setConfig(signLockConfig);
        lockService.clearLookupCache();
        lockListener.setConfig(signLockConfig);
        lockManagementGuiService.setConfig(signLockConfig);
        lockManagementGuiService.setGuiConfig(lockGuiConfig);
        lockManagementGuiActionService.setConfig(signLockConfig);
        lockManagementGuiActionService.setGuiConfig(lockGuiConfig);
        getLogger().info("SignLock configuration reloaded.");
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

    private void saveDefaultGuiConfig() {
        File guiFile = new File(getDataFolder(), "gui.yml");
        if (!guiFile.exists()) {
            saveResource("gui.yml", false);
        }
    }

    private LockGuiConfig loadGuiConfig() {
        File guiFile = new File(getDataFolder(), "gui.yml");
        return new LockGuiConfig(YamlConfiguration.loadConfiguration(guiFile));
    }
}
