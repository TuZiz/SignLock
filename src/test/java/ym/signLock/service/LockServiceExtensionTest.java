package ym.signLock.service;

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
import org.mockito.Mockito;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import ym.signLock.config.SignLockConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LockServiceExtensionTest {

    private ServerMock server;
    private WorldMock world;
    private LockService lockService;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        lockService = new LockService(createConfig(), Mockito.mock(PlayerIdentityService.class));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void extensionOnEitherHalfIsDiscoveredForSharedTarget() {
        Block leftHalf = world.getBlockAt(0, 64, 0);
        Block rightHalf = world.getBlockAt(1, 64, 0);
        configureDoubleChest(leftHalf, rightHalf, BlockFace.NORTH);
        placeWallSign(leftHalf, BlockFace.NORTH, "[private]", "Owner", "", "");
        Sign extension = placeWallSign(rightHalf, BlockFace.SOUTH, "[more users]", "Alice", "", "");

        LockService.LockInfo leftLock = lockService.findLock(leftHalf);
        LockService.LockInfo rightLock = lockService.findLock(rightHalf);
        LockService.LockInfo extensionLock = lockService.findManagedSignLock(extension.getBlock());

        assertNotNull(leftLock);
        assertNotNull(rightLock);
        assertNotNull(extensionLock);
        assertTrue(leftLock.allowedPlayers().contains("Alice"));
        assertTrue(rightLock.allowedPlayers().contains("Alice"));
        assertEquals(coords(leftLock.targetBlock()), coords(rightLock.targetBlock()));
        assertEquals(coords(leftLock.targetBlock()), coords(extensionLock.targetBlock()));
    }

    @Test
    void addPlayerAndCapacityChecksUseCanonicalTargetNotClickedHalf() {
        Block leftHalf = world.getBlockAt(10, 64, 0);
        Block rightHalf = world.getBlockAt(11, 64, 0);
        configureDoubleChest(leftHalf, rightHalf, BlockFace.NORTH);
        placeWallSign(leftHalf, BlockFace.NORTH, "[private]", "Owner", "", "");
        Sign preferredExtension = placeWallSign(rightHalf, BlockFace.SOUTH, "[more users]", "", "", "");

        LockService.LockInfo lockFromLeft = lockService.findLock(leftHalf);
        LockService.LockInfo lockFromRight = lockService.findLock(rightHalf);
        LockService.AddPlayerResult addResult = lockService.addPlayerToLock(preferredExtension, lockFromRight, "Bob");

        assertNotNull(lockFromLeft);
        assertNotNull(lockFromRight);
        assertEquals(LockService.AddPlayerResult.ADDED, addResult);
        assertTrue(lockService.canCreateMoreUsersSign(leftHalf));
        assertTrue(lockService.canCreateMoreUsersSign(rightHalf));

        Sign updated = (Sign) preferredExtension.getBlock().getState();
        assertEquals("Bob", updated.getLine(1));
        assertTrue(lockService.findLock(leftHalf).allowedPlayers().contains("Bob"));
    }

    private SignLockConfig createConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", java.util.List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", java.util.List.of("CHEST"));
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
        return sign;
    }

    private String coords(Block block) {
        return block.getX() + "," + block.getY() + "," + block.getZ();
    }
}
