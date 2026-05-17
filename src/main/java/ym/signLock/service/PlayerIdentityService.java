package ym.signLock.service;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
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
    private boolean dirty;

    public PlayerIdentityService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        this.storage = YamlConfiguration.loadConfiguration(file);
    }

    public void preload(OfflinePlayer[] offlinePlayers) {
        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            remember(offlinePlayer);
        }
        saveIfDirty();
    }

    public boolean remember(OfflinePlayer player) {
        String name = player.getName();
        if (name == null || name.isBlank() || player.getUniqueId() == null) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        String namePath = "names." + normalizeName(name);
        String uuidPath = "uuids." + uuid;
        boolean changed = !uuid.toString().equals(storage.getString(namePath))
                || !name.equals(storage.getString(uuidPath));
        if (!changed) {
            return false;
        }

        storage.set(namePath, uuid.toString());
        storage.set(uuidPath, name);
        dirty = true;
        return true;
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
        return storage.getString("uuids." + uuid);
    }

    public void save() {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            storage.save(file);
            dirty = false;
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to save players.yml: " + exception.getMessage());
        }
    }

    public void saveIfDirty() {
        if (dirty) {
            save();
        }
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
}
