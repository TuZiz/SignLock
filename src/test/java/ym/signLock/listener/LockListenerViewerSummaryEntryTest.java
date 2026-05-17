package ym.signLock.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.mockito.Mockito;
import ym.signLock.config.SignLockConfig;
import ym.signLock.gui.LockManagementGuiService;
import ym.signLock.service.LockService;
import ym.signLock.service.PlayerIdentityService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LockListenerViewerSummaryEntryTest {

    private ServerMock server;
    private WorldMock world;
    private SignLockConfig config;
    private PlayerIdentityService playerIdentityService;
    private LockManagementGuiService guiService;
    private LockListener listener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        config = createConfig();
        playerIdentityService = Mockito.mock(PlayerIdentityService.class);
        guiService = Mockito.mock(LockManagementGuiService.class);
        listener = new LockListener(new LockService(config, playerIdentityService), playerIdentityService, config, guiService);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void authorizedNonOwnerNormalRightClickOpensReadOnlySummaryGui() {
        Block chest = world.getBlockAt(0, 64, 0);
        chest.setType(Material.CHEST);
        Sign sign = placeManagedWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Guest", "");

        Player viewer = mockPlayer("Guest", false);
        PlayerInteractEvent event = new PlayerInteractEvent(viewer, Action.RIGHT_CLICK_BLOCK, null, sign.getBlock(), BlockFace.UP);

        listener.onPlayerInteract(event);

        assertTrue(event.isCancelled());
        verify(guiService).openFor(viewer, sign);
        verify(viewer, never()).openSign(any(Sign.class));
    }

    @Test
    void unauthorizedPlayerDoesNotReceiveSummaryGui() {
        Block chest = world.getBlockAt(10, 64, 0);
        chest.setType(Material.CHEST);
        Sign sign = placeManagedWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Guest", "");

        Player intruder = mockPlayer("Intruder", false);
        PlayerInteractEvent event = new PlayerInteractEvent(intruder, Action.RIGHT_CLICK_BLOCK, null, sign.getBlock(), BlockFace.UP);

        listener.onPlayerInteract(event);

        assertTrue(event.isCancelled());
        verify(guiService, never()).openFor(any(Player.class), any(Sign.class));
        verify(intruder, never()).openSign(any(Sign.class));
        verify(intruder).sendMessage(config.protectedSignMessage());
    }

    private SignLockConfig createConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", List.of("CHEST", "BARREL"));
        return new SignLockConfig(yaml);
    }

    private Player mockPlayer(String name, boolean sneaking) {
        Player player = Mockito.mock(Player.class);
        when(player.getName()).thenReturn(name);
        when(player.getUniqueId()).thenReturn(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        when(player.hasPermission(anyString())).thenReturn(false);
        when(player.isSneaking()).thenReturn(sneaking);
        return player;
    }

    private Sign placeManagedWallSign(Block target, BlockFace signFace, String... lines) {
        Block signBlock = target.getRelative(signFace);
        signBlock.setType(Material.OAK_WALL_SIGN);
        Directional directional = (Directional) signBlock.getBlockData();
        directional.setFacing(signFace);
        signBlock.setBlockData(directional, false);
        Sign sign = (Sign) signBlock.getState();
        for (int index = 0; index < lines.length; index++) {
            sign.setLine(index, lines[index]);
        }
        sign.update(true, false);
        return (Sign) signBlock.getState();
    }
}
