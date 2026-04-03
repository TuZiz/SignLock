package ym.signLock.service;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.mockito.Mockito;
import ym.signLock.config.SignLockConfig;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class LockServiceSummarySemanticsTest {

    private ServerMock server;
    private WorldMock world;
    private LockService lockService;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        SignLockConfig config = createConfig();
        PlayerIdentityService playerIdentityService = Mockito.mock(PlayerIdentityService.class);
        lockService = new LockService(config, playerIdentityService);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void describeLockReturnsSingleChestTargetSummary() {
        Block chest = world.getBlockAt(0, 64, 0);
        chest.setType(Material.CHEST);
        Sign primary = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Alice", "");

        LockService.LockDetails details = lockService.describeLock(primary.getBlock());

        assertNotNull(details);
        assertNotNull(details.target());
        assertEquals(LockService.LockTargetKind.SINGLE_CHEST, details.target().kind());
        assertEquals(0, details.target().x());
        assertEquals(64, details.target().y());
        assertEquals(0, details.target().z());
    }

    @Test
    void describeLockUsesCanonicalDoubleChestTargetForPrimaryAndExtensionSigns() {
        Block leftHalf = world.getBlockAt(10, 64, 0);
        Block rightHalf = world.getBlockAt(11, 64, 0);
        configureDoubleChest(leftHalf, rightHalf, BlockFace.NORTH);
        Sign primary = placeWallSign(rightHalf, BlockFace.NORTH, "[private]", "Owner", "Alice", "");
        Sign extension = lockService.createMoreUsersSign(leftHalf, Material.OAK_WALL_SIGN);
        assertNotNull(extension);

        LockService.LockDetails primaryDetails = lockService.describeLock(primary.getBlock());
        LockService.LockDetails extensionDetails = lockService.describeLock(extension.getBlock());

        assertNotNull(primaryDetails);
        assertNotNull(extensionDetails);
        assertEquals(LockService.LockTargetKind.DOUBLE_CHEST, primaryDetails.target().kind());
        assertEquals(primaryDetails.target(), extensionDetails.target());
    }

    @Test
    void viewerScopeDifferentiatesManageAccessAndDenied() {
        Block chest = world.getBlockAt(20, 64, 0);
        chest.setType(Material.CHEST);
        Sign primary = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Guest", "");
        LockService.LockInfo lock = lockService.findManagedSignLock(primary.getBlock());
        assertNotNull(lock);

        assertEquals(LockService.LockViewerScope.MANAGE, lockService.viewerScope(lock, mockPlayer("Owner")));
        assertEquals(LockService.LockViewerScope.ACCESS, lockService.viewerScope(lock, mockPlayer("Guest")));
        assertEquals(LockService.LockViewerScope.DENIED, lockService.viewerScope(lock, mockPlayer("Intruder")));
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

    private Player mockPlayer(String name) {
        Player player = Mockito.mock(Player.class);
        when(player.getName()).thenReturn(name);
        when(player.getUniqueId()).thenReturn(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        when(player.hasPermission("signlock.admin")).thenReturn(false);
        return player;
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
