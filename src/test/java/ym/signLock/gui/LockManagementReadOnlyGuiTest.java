package ym.signLock.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.mockito.Mockito;
import ym.signLock.config.SignLockConfig;
import ym.signLock.service.LockService;
import ym.signLock.service.PlayerIdentityService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LockManagementReadOnlyGuiTest {

    private ServerMock server;
    private WorldMock world;
    private SignLockConfig config;
    private LockService lockService;
    private LockManagementGuiService guiService;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        config = createConfig();
        PlayerIdentityService playerIdentityService = Mockito.mock(PlayerIdentityService.class);
        lockService = new LockService(config, playerIdentityService);
        guiService = new LockManagementGuiService(lockService, config);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void readOnlyInventoryReplacesManageControlsWithSummaryPlaceholders() {
        Block chest = world.getBlockAt(0, 64, 0);
        chest.setType(Material.CHEST);
        Sign primary = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Alice", "");

        LockService.LockInfo lock = lockService.findManagedSignLock(primary.getBlock());
        LockService.LockDetails details = lockService.describeLock(primary.getBlock());
        LockManagementGuiHolder holder = new LockManagementGuiHolder(
                LockManagementSession.from(lock),
                LockSummaryView.from(lock, details, LockService.LockViewerScope.ACCESS)
        );
        var inventory = guiService.buildInventory(holder);

        assertNotNull(inventory.getItem(LockManagementGui.ADD_SLOT));
        assertEquals("Read only", ChatColor.stripColor(inventory.getItem(LockManagementGui.ADD_SLOT).getItemMeta().getDisplayName()));
        assertEquals("Read only", ChatColor.stripColor(inventory.getItem(LockManagementGui.REMOVE_SELECTED_SLOT).getItemMeta().getDisplayName()));
        assertEquals("Alice", ChatColor.stripColor(inventory.getItem(LockManagementGui.PLAYER_SLOTS[0]).getItemMeta().getDisplayName()));
        assertEquals("Access", ChatColor.stripColor(inventory.getItem(LockManagementGui.SCOPE_SLOT).getItemMeta().getDisplayName()));
    }

    private SignLockConfig createConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", List.of("CHEST", "BARREL"));
        yaml.set("messages.gui-scope-label", "%scope%");
        yaml.set("messages.scope-access-label", "Access");
        yaml.set("messages.gui-read-only-button", "Read only");
        return new SignLockConfig(yaml);
    }

    private Sign placeWallSign(Block target, BlockFace signFace, String... lines) {
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
