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
import ym.signLock.config.LockGuiConfig;
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
    private LockGuiConfig guiConfig;
    private LockService lockService;
    private LockManagementGuiService guiService;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        config = createConfig();
        guiConfig = createGuiConfig();
        PlayerIdentityService playerIdentityService = Mockito.mock(PlayerIdentityService.class);
        lockService = new LockService(config, playerIdentityService);
        guiService = new LockManagementGuiService(lockService, config, guiConfig);
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
        assertEquals("Owner Owner",
                ChatColor.stripColor(inventory.getItem(LockManagementGui.OWNER_SLOT).getItemMeta().getDisplayName()));
        assertEquals("Alice", ChatColor.stripColor(inventory.getItem(LockManagementGui.PLAYER_SLOTS[0]).getItemMeta().getDisplayName()));
        assertEquals("Bob", ChatColor.stripColor(inventory.getItem(LockManagementGui.PLAYER_SLOTS[1]).getItemMeta().getDisplayName()));
        assertEquals("Add",
                ChatColor.stripColor(inventory.getItem(LockManagementGui.ADD_SLOT).getItemMeta().getDisplayName()));
        assertEquals("Close",
                ChatColor.stripColor(inventory.getItem(LockManagementGui.CLOSE_SLOT).getItemMeta().getDisplayName()));
        assertEquals("Scope Manage",
                ChatColor.stripColor(inventory.getItem(LockManagementGui.SCOPE_SLOT).getItemMeta().getDisplayName()));
    }

    @Test
    void inventoryUsesHumanReadableDoubleChestTargetSummary() {
        Block leftHalf = world.getBlockAt(20, 64, 0);
        Block rightHalf = world.getBlockAt(21, 64, 0);
        configureDoubleChest(leftHalf, rightHalf, BlockFace.NORTH);
        Sign primary = placeWallSign(rightHalf, BlockFace.NORTH, "[private]", "Owner", "Alice", "");

        LockManagementGuiHolder holder = guiService.createHolder(primary.getBlock());
        assertNotNull(holder);

        var inventory = guiService.buildInventory(holder);
        assertEquals(
                "Target Double chest world 20 64 0",
                ChatColor.stripColor(inventory.getItem(LockManagementGui.TARGET_SLOT).getItemMeta().getDisplayName())
        );
    }

    @Test
    void inventoryUsesConfiguredGuiLayoutAndItemsFromGuiYaml() {
        YamlConfiguration guiYaml = new YamlConfiguration();
        guiYaml.set("gui.title", "&1Custom Lock Panel");
        guiYaml.set("gui.layout", List.of(
                "#####SOT#",
                "#########",
                "IIIBRFC##"
        ));
        guiYaml.set("gui.items.owner.name", "Owner %owner%");
        guiYaml.set("gui.items.add.material", "EMERALD");
        guiYaml.set("gui.items.add.name", "&aCustom Add");
        guiYaml.set("gui.items.player.material", "MAP");
        guiYaml.set("gui.items.player.name", "&bPlayer %player%");

        guiService = new LockManagementGuiService(lockService, config, new LockGuiConfig(guiYaml));

        Block chest = world.getBlockAt(30, 64, 0);
        chest.setType(Material.CHEST);
        Sign primary = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Alice", "");

        LockManagementGuiHolder holder = guiService.createHolder(primary.getBlock());
        assertNotNull(holder);

        var inventory = guiService.buildInventory(holder);
        assertEquals("Owner Owner", ChatColor.stripColor(inventory.getItem(6).getItemMeta().getDisplayName()));
        assertEquals(Material.EMERALD, inventory.getItem(21).getType());
        assertEquals("Custom Add", ChatColor.stripColor(inventory.getItem(21).getItemMeta().getDisplayName()));
        assertEquals(Material.MAP, inventory.getItem(18).getType());
        assertEquals("Player Alice", ChatColor.stripColor(inventory.getItem(18).getItemMeta().getDisplayName()));
    }

    private SignLockConfig createConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", List.of("CHEST", "TRAPPED_CHEST", "BARREL"));
        yaml.set("messages.gui-scope-label", "Scope %scope%");
        yaml.set("messages.scope-manage-label", "Manage");
        yaml.set("messages.target-summary-double-chest", "Double chest %world% %x% %y% %z%");
        yaml.set("messages.gui-target-summary-label", "Target %target%");
        return new SignLockConfig(yaml);
    }

    private LockGuiConfig createGuiConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("gui.title", "&8Test GUI");
        yaml.set("gui.items.owner.name", "Owner %owner%");
        yaml.set("gui.items.target.name", "Target %target%");
        yaml.set("gui.items.extensions.name", "Extensions %count%");
        yaml.set("gui.items.scope-manage.name", "Scope %scope%");
        yaml.set("gui.items.scope-read-only.name", "Scope %scope%");
        yaml.set("gui.items.player.name", "%player%");
        yaml.set("gui.items.player-selected.name", "%player%");
        yaml.set("gui.items.player-read-only.name", "%player%");
        yaml.set("gui.items.add.name", "Add");
        yaml.set("gui.items.close.name", "Close");
        yaml.set("gui.items.refresh.name", "Refresh");
        yaml.set("gui.items.read-only-action.name", "Read only");
        return new LockGuiConfig(yaml);
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
