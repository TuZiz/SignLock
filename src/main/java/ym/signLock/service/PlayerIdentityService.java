package ym.signLock.service;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

public final class PlayerIdentityService {

    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration storage;

    public PlayerIdentityService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        this.storage = YamlConfiguration.loadConfiguration(file);
    }

    public void preload(OfflinePlayer[] offlinePlayers) {
        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            remember(offlinePlayer);
        }
        save();
    }

    public void remember(OfflinePlayer player) {
        String name = player.getName();
        if (name == null || name.isBlank() || player.getUniqueId() == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        storage.set("names." + normalizeName(name), uuid.toString());
        storage.set("uuids." + uuid, name);
    }

    public UUID findUuidByName(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return null;
        }

        String raw = storage.getString("names." + normalizeName(playerName));
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
        if (uuid == null) {
            return playerName;
        }

        String lastKnownName = getLastKnownName(uuid);
        return lastKnownName == null || lastKnownName.isBlank() ? playerName : lastKnownName;
    }

    public String getLastKnownName(UUID uuid) {
        return storage.getString("uuids." + uuid);
    }

    public void save() {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            storage.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to save players.yml: " + exception.getMessage());
        }
    }

    private static String normalizeName(String playerName) {
        return playerName.toLowerCase(Locale.ROOT);
    }
}
