package ym.signLock.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LockManagementGuiViewTest {

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
    void createHolderUsesCanonicalTargetAndSummaryData() {
        Block leftHalf = world.getBlockAt(0, 64, 0);
        Block rightHalf = world.getBlockAt(1, 64, 0);
        configureDoubleChest(leftHalf, rightHalf, BlockFace.NORTH);
        Sign primary = placeWallSign(rightHalf, BlockFace.NORTH, "[private]", "Owner", "Alice", "Charlie");
        Sign extension = lockService.createMoreUsersSign(leftHalf, Material.OAK_WALL_SIGN);
        assertNotNull(extension);
        extension.setLine(1, "Bob");
        extension.update(true, false);

        LockManagementGuiHolder holder = guiService.createHolder(primary.getBlock());
        assertNotNull(holder);
        assertEquals(0, holder.session().x());
        assertEquals(64, holder.session().y());
        assertEquals(0, holder.session().z());
        assertEquals("Owner", holder.view().owner());
        assertEquals(Set.of("Alice", "Bob", "Charlie"), Set.copyOf(holder.view().allowedPlayers()));
        assertEquals(1, holder.view().extensionCount());
    }

    @Test
    void inventoryRendersSummaryAndPlayerSlots() {
        Block chest = world.getBlockAt(10, 64, 0);
        chest.setType(Material.CHEST);
        Sign primary = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Alice", "Bob");

        LockManagementGuiHolder holder = guiService.createHolder(primary.getBlock());
        assertNotNull(holder);

        var inventory = guiService.buildInventory(holder);
        assertEquals(ChatColor.stripColor(config.guiOwnerLabel("Owner")),
                ChatColor.stripColor(inventory.getItem(LockManagementGui.OWNER_SLOT).getItemMeta().getDisplayName()));
        assertEquals("Alice", ChatColor.stripColor(inventory.getItem(LockManagementGui.PLAYER_SLOTS[0]).getItemMeta().getDisplayName()));
        assertEquals("Bob", ChatColor.stripColor(inventory.getItem(LockManagementGui.PLAYER_SLOTS[1]).getItemMeta().getDisplayName()));
        assertEquals(ChatColor.stripColor(config.guiAddButtonLabel()),
                ChatColor.stripColor(inventory.getItem(LockManagementGui.ADD_SLOT).getItemMeta().getDisplayName()));
        assertEquals(ChatColor.stripColor(config.guiCloseButtonLabel()),
                ChatColor.stripColor(inventory.getItem(LockManagementGui.CLOSE_SLOT).getItemMeta().getDisplayName()));
    }

    private SignLockConfig createConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", List.of("CHEST", "TRAPPED_CHEST", "BARREL"));
        return new SignLockConfig(yaml);
    }

    private void configureDoubleChest(Block leftHalf, Block rightHalf, BlockFace facing) {
        leftHalf.setType(Material.CHEST);
        rightHalf.setType(Material.CHEST);

        Chest leftData = (Chest) Material.CHEST.createBlockData();
        leftData.setFacing(facing);
        leftData.setType(Chest.Type.LEFT);
        leftHalf.setBlockData(leftData, false);

        Chest rightData = (Chest) Material.CHEST.createBlockData();
        rightData.setFacing(facing);
        rightData.setType(Chest.Type.RIGHT);
        rightHalf.setBlockData(rightData, false);
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
