package ym.signLock.service;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class PlayerIdentityServiceTest {

    @TempDir
    Path tempDir;

    private JavaPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = Mockito.mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tempDir.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("PlayerIdentityServiceTest"));
    }

    @Test
    void rememberAndPreloadPersistBidirectionalIndexesAcrossReload() {
        PlayerIdentityService service = new PlayerIdentityService(plugin);
        OfflinePlayer alpha = offlinePlayer("Alpha", UUID.fromString("00000000-0000-0000-0000-000000000001"));
        OfflinePlayer beta = offlinePlayer("Beta", UUID.fromString("00000000-0000-0000-0000-000000000002"));

        service.remember(alpha);
        service.preload(new OfflinePlayer[]{beta});

        PlayerIdentityService reloaded = new PlayerIdentityService(plugin);
        assertEquals(alpha.getUniqueId(), reloaded.findUuidByName("alpha"));
        assertEquals(beta.getUniqueId(), reloaded.findUuidByName("BETA"));
        assertEquals("Alpha", reloaded.getLastKnownName(alpha.getUniqueId()));
        assertEquals("Beta", reloaded.getLastKnownName(beta.getUniqueId()));
        assertTrue(tempDir.resolve("players.yml").toFile().isFile());
    }

    @Test
    void findUuidByNameIgnoresCaseAndResolveStoredNameUsesLatestKnownRename() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-00000000000a");
        PlayerIdentityService service = new PlayerIdentityService(plugin);

        service.remember(offlinePlayer("OriginalName", uuid));
        service.remember(offlinePlayer("CurrentName", uuid));
        service.save();

        PlayerIdentityService reloaded = new PlayerIdentityService(plugin);
        assertEquals(uuid, reloaded.findUuidByName("originalname"));
        assertEquals(uuid, reloaded.findUuidByName("CURRENTNAME"));
        assertEquals("CurrentName", reloaded.resolveStoredName("OriginalName"));
        assertEquals("CurrentName", reloaded.resolveStoredName("currentname"));
    }

    @Test
    void resolveStoredNamePreservesUnknownInput() {
        PlayerIdentityService service = new PlayerIdentityService(plugin);

        assertNull(service.findUuidByName("Nobody"));
        assertEquals("Nobody", service.resolveStoredName("Nobody"));
    }

    private OfflinePlayer offlinePlayer(String name, UUID uuid) {
        OfflinePlayer player = Mockito.mock(OfflinePlayer.class);
        when(player.getName()).thenReturn(name);
        when(player.getUniqueId()).thenReturn(uuid);
        return player;
    }
}
