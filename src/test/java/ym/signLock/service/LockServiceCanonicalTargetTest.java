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
import static org.junit.jupiter.api.Assertions.assertSame;

class LockServiceCanonicalTargetTest {

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
    void doubleChestBothHalvesResolveToSameCanonicalTarget() {
        Block leftHalf = world.getBlockAt(0, 64, 0);
        Block rightHalf = world.getBlockAt(1, 64, 0);
        configureDoubleChest(leftHalf, rightHalf, BlockFace.NORTH);
        Sign sign = placeWallSign(leftHalf, BlockFace.NORTH, "[private]", "Owner", "", "");

        Block leftPlacement = lockService.findPlacementTarget(world.getBlockAt(0, 64, -1));
        Block rightPlacement = lockService.findPlacementTarget(world.getBlockAt(1, 64, -1));
        Block leftDirect = lockService.resolveDirectLockTarget(leftHalf);
        Block rightDirect = lockService.resolveDirectLockTarget(rightHalf);
        LockService.LockInfo leftLock = lockService.findLock(leftHalf);
        LockService.LockInfo rightLock = lockService.findLock(rightHalf);

        assertNotNull(leftPlacement);
        assertEquals(coords(leftPlacement), coords(rightPlacement));
        assertEquals(coords(leftDirect), coords(rightDirect));
        assertNotNull(leftLock);
        assertNotNull(rightLock);
        assertEquals("Owner", leftLock.owner());
        assertEquals("Owner", rightLock.owner());
        assertEquals(coords(leftLock.targetBlock()), coords(rightLock.targetBlock()));
        assertSame(sign.getBlock(), leftLock.signBlock());
    }

    @Test
    void barrelPlacementAndLookupRemainStable() {
        Block barrel = world.getBlockAt(10, 64, 0);
        barrel.setType(Material.BARREL);
        placeWallSign(barrel, BlockFace.NORTH, "[private]", "Owner", "Alice", "");

        Block placement = lockService.findPlacementTarget(world.getBlockAt(10, 64, -1));
        Block direct = lockService.resolveDirectLockTarget(barrel);
        LockService.LockInfo lock = lockService.findLock(barrel);

        assertEquals(coords(barrel), coords(placement));
        assertEquals(coords(barrel), coords(direct));
        assertNotNull(lock);
        assertEquals(coords(barrel), coords(lock.targetBlock()));
        assertEquals("Owner", lock.owner());
        assertEquals(1, lock.allowedPlayers().size());
    }

    @Test
    void shulkerPlacementAndLookupRemainStable() {
        Block shulker = world.getBlockAt(20, 64, 0);
        shulker.setType(Material.SHULKER_BOX);
        placeWallSign(shulker, BlockFace.NORTH, "[private]", "Owner", "", "");

        Block placement = lockService.findPlacementTarget(world.getBlockAt(20, 64, -1));
        Block direct = lockService.resolveDirectLockTarget(shulker);
        LockService.LockInfo lock = lockService.findLock(shulker);

        assertEquals(coords(shulker), coords(placement));
        assertEquals(coords(shulker), coords(direct));
        assertNotNull(lock);
        assertEquals(coords(shulker), coords(lock.targetBlock()));
    }

    private SignLockConfig createConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", java.util.List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", java.util.List.of("CHEST", "BARREL", "SHULKER_BOX"));
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
