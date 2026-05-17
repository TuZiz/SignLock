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
        Sign extension = placeWallSign(rightHalf, BlockFace.NORTH, "[more users]", "Alice", "", "");

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
        Sign preferredExtension = placeWallSign(rightHalf, BlockFace.NORTH, "[more users]", "", "", "");

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

    @Test
    void legacyEnglishHeadersRemainReadableAndWritableUnderChineseConfig() {
        lockService = new LockService(createChineseConfig(), Mockito.mock(PlayerIdentityService.class));

        Block chest = world.getBlockAt(20, 64, 0);
        chest.setType(Material.CHEST);
        Sign primary = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Alice", "");
        Sign extension = lockService.createMoreUsersSign(chest, Material.OAK_WALL_SIGN);
        assertNotNull(extension);
        extension.setLine(0, "[more users]");
        extension.setLine(1, "");
        extension.setLine(2, "");
        extension.setLine(3, "");
        extension.update(true, false);

        LockService.LockInfo lock = lockService.findManagedSignLock(primary.getBlock());
        LockService.LockInfo extensionLock = lockService.findManagedSignLock(extension.getBlock());
        assertNotNull(lock);
        assertNotNull(extensionLock);

        LockService.AddPlayerResult addResult = lockService.addPlayerToLock(extension, lock, "Carol");

        assertTrue(lock.allowedPlayers().contains("Alice"));
        assertEquals(LockService.AddPlayerResult.ADDED, addResult);

        Sign updatedExtension = (Sign) extension.getBlock().getState();
        assertEquals("Carol", updatedExtension.getLine(1));
    }

    @Test
    void extensionCreationFallsBackToUpFaceWhenHorizontalFacesAreBlocked() {
        lockService = new LockService(createSixFaceConfig(), Mockito.mock(PlayerIdentityService.class));

        Block chest = world.getBlockAt(30, 64, 0);
        chest.setType(Material.CHEST);
        placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "", "");

        chest.getRelative(BlockFace.NORTH).setType(Material.STONE);
        chest.getRelative(BlockFace.SOUTH).setType(Material.STONE);
        chest.getRelative(BlockFace.EAST).setType(Material.STONE);
        chest.getRelative(BlockFace.WEST).setType(Material.STONE);

        Sign extension = lockService.createMoreUsersSign(chest, Material.OAK_WALL_SIGN);

        assertNotNull(extension);
        assertEquals(chest.getY() + 1, extension.getBlock().getY());
        assertEquals("[more users]", extension.getLine(0));
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

    private SignLockConfig createChineseConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[锁]");
        yaml.set("signs.more-users-header", "[更多用户]");
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", java.util.List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", java.util.List.of("CHEST"));
        return new SignLockConfig(yaml);
    }

    private SignLockConfig createSixFaceConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.max-more-user-signs", 6);
        yaml.set("protection.extension-placement-order", java.util.List.of("NORTH", "SOUTH", "EAST", "WEST", "UP", "DOWN"));
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
        return (Sign) signBlock.getState();
    }

    private String coords(Block block) {
        return block.getX() + "," + block.getY() + "," + block.getZ();
    }
}
