package ym.signLock.service;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import ym.signLock.config.SignLockConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LockServiceLookupCacheTest {

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
    void nonSignManagedLookupDoesNotReadBlockState() {
        Block block = Mockito.mock(Block.class);
        when(block.getType()).thenReturn(Material.STONE);

        assertNull(lockService.findManagedSignLock(block));

        verify(block).getType();
        Mockito.verify(block, Mockito.never()).getState();
    }

    @Test
    void signMaterialHelperSupportsWallAndHangingSigns() {
        assertNotNull(Material.OAK_WALL_SIGN);
        assertTrue(LockService.isSignMaterial(Material.OAK_SIGN));
        assertTrue(LockService.isSignMaterial(Material.OAK_WALL_SIGN));
        assertTrue(LockService.isSignMaterial(Material.OAK_HANGING_SIGN));
        assertTrue(LockService.isSignMaterial(Material.OAK_WALL_HANGING_SIGN));
        assertFalse(LockService.isSignMaterial(Material.CHEST));
    }

    @Test
    void nullLookupIsCachedUntilInvalidated() {
        Block chest = world.getBlockAt(0, 64, 0);
        chest.setType(Material.CHEST);

        assertNull(lockService.findLock(chest));

        placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "", "");
        assertNull(lockService.findLock(chest));

        lockService.invalidateLookupCache(chest);
        assertNotNull(lockService.findLock(chest));
    }

    @Test
    void setConfigClearsLookupCacheForReload() {
        Block chest = world.getBlockAt(10, 64, 0);
        chest.setType(Material.CHEST);

        assertNull(lockService.findLock(chest));

        placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "", "");
        lockService.setConfig(createConfig());

        assertNotNull(lockService.findLock(chest));
    }

    @Test
    void automationMoveBlocksWhenEitherSideIsProtected() {
        Block protectedChest = world.getBlockAt(20, 64, 0);
        protectedChest.setType(Material.CHEST);
        placeWallSign(protectedChest, BlockFace.NORTH, "[private]", "Owner", "", "");

        Block plainChest = world.getBlockAt(30, 64, 0);
        plainChest.setType(Material.CHEST);

        assertTrue(lockService.shouldBlockAutomationMove(protectedChest, plainChest));
        assertTrue(lockService.shouldBlockAutomationMove(plainChest, protectedChest));
        assertFalse(lockService.shouldBlockAutomationMove(plainChest, null));
        assertFalse(lockService.shouldBlockAutomationMove(null, null));
    }

    private SignLockConfig createConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", List.of("CHEST"));
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
