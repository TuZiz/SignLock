package ym.signLock.service;

import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class PlayerIdentityService {

    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration storage;
    private final Object storageLock = new Object();
    private final Object saveLock = new Object();
    private boolean dirty;
    private long dirtyVersion;
    private ScheduledExecutorService autoSaveExecutor;

    public PlayerIdentityService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        this.storage = YamlConfiguration.loadConfiguration(file);
    }

    public void startAutoSave(long periodSeconds) {
        if (periodSeconds <= 0) {
            throw new IllegalArgumentException("periodSeconds must be positive");
        }

        synchronized (saveLock) {
            if (autoSaveExecutor != null) {
                return;
            }
            autoSaveExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "SignLock-PlayerIdentitySave");
                thread.setDaemon(true);
                return thread;
            });
            autoSaveExecutor.scheduleWithFixedDelay(this::saveIfDirty, periodSeconds, periodSeconds, TimeUnit.SECONDS);
        }
    }

    public void shutdown() {
        ScheduledExecutorService executor;
        synchronized (saveLock) {
            executor = autoSaveExecutor;
            autoSaveExecutor = null;
        }
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException exception) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        save();
    }

    public boolean remember(Player player) {
        String name = player.getName();
        if (name == null || name.isBlank() || player.getUniqueId() == null) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        String namePath = "names." + normalizeName(name);
        String uuidPath = "uuids." + uuid;
        synchronized (storageLock) {
            boolean changed = !uuid.toString().equals(storage.getString(namePath))
                    || !name.equals(storage.getString(uuidPath));
            if (!changed) {
                return false;
            }

            storage.set(namePath, uuid.toString());
            storage.set(uuidPath, name);
            dirty = true;
            dirtyVersion++;
            return true;
        }
    }

    public UUID findUuidByName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return null;
        }

        String raw;
        synchronized (storageLock) {
            raw = storage.getString("names." + normalizeName(playerName));
        }
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public String resolveStoredName(String playerName) {
        UUID uuid = findUuidByName(playerName);
        if (uuid != null) {
            Server server = plugin.getServer();
            if (server != null) {
                Player onlineByUuid = server.getPlayer(uuid);
                if (onlineByUuid != null) {
                    remember(onlineByUuid);
                }
            }

            String lastKnownName = getLastKnownName(uuid);
            return lastKnownName == null || lastKnownName.isBlank() ? playerName : lastKnownName;
        }

        Player onlineByName = findOnlinePlayer(playerName);
        if (onlineByName == null) {
            return playerName;
        }

        remember(onlineByName);
        return onlineByName.getName();
    }

    public String getLastKnownName(UUID uuid) {
        synchronized (storageLock) {
            return storage.getString("uuids." + uuid);
        }
    }

    public void save() {
        StorageSnapshot snapshot = createSnapshotIfDirty();
        if (snapshot == null) {
            return;
        }

        synchronized (saveLock) {
            YamlConfiguration snapshotStorage = new YamlConfiguration();
            for (Map.Entry<String, String> entry : snapshot.names().entrySet()) {
                snapshotStorage.set("names." + entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, String> entry : snapshot.uuids().entrySet()) {
                snapshotStorage.set("uuids." + entry.getKey(), entry.getValue());
            }

            try {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                snapshotStorage.save(file);
                markSaved(snapshot.version());
            } catch (IOException exception) {
                plugin.getLogger().log(Level.WARNING, "Failed to save players.yml", exception);
            }
        }
    }

    public void saveIfDirty() {
        save();
    }

    private StorageSnapshot createSnapshotIfDirty() {
        synchronized (storageLock) {
            if (!dirty) {
                return null;
            }
            return new StorageSnapshot(
                    dirtyVersion,
                    copySectionValues(storage.getConfigurationSection("names")),
                    copySectionValues(storage.getConfigurationSection("uuids"))
            );
        }
    }

    private void markSaved(long savedVersion) {
        synchronized (storageLock) {
            if (dirtyVersion == savedVersion) {
                dirty = false;
            }
        }
    }

    private Map<String, String> copySectionValues(ConfigurationSection section) {
        if (section == null) {
            return Map.of();
        }

        Map<String, String> values = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            String value = section.getString(key);
            if (value != null) {
                values.put(key, value);
            }
        }
        return values;
    }

    private Player findOnlinePlayer(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return null;
        }

        Server server = plugin.getServer();
        if (server == null) {
            return null;
        }

        Player exact = server.getPlayerExact(playerName);
        if (exact != null) {
            return exact;
        }

        for (Player onlinePlayer : server.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(playerName)) {
                return onlinePlayer;
            }
        }
        return null;
    }

    private static String normalizeName(String playerName) {
        return playerName.toLowerCase(Locale.ROOT);
    }

    private record StorageSnapshot(long version, Map<String, String> names, Map<String, String> uuids) {
    }
}
