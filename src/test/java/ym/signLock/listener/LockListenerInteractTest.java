package ym.signLock.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
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

class LockListenerInteractTest {

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
    void unauthorizedPlayerIsBlockedFromEitherDoubleChestHalf() {
        Block leftHalf = world.getBlockAt(0, 64, 0);
        Block rightHalf = world.getBlockAt(1, 64, 0);
        configureDoubleChest(leftHalf, rightHalf, BlockFace.NORTH);
        placeWallSign(leftHalf, BlockFace.NORTH, "[private]", "Owner", "", "");

        Player leftIntruder = mockPlayer("IntruderLeft");
        PlayerInteractEvent leftEvent = new PlayerInteractEvent(leftIntruder, Action.RIGHT_CLICK_BLOCK, null, leftHalf, BlockFace.UP);
        listener.onPlayerInteract(leftEvent);

        assertTrue(leftEvent.isCancelled());
        verify(leftIntruder).sendMessage(config.lockedUseMessage());

        Player rightIntruder = mockPlayer("IntruderRight");
        PlayerInteractEvent rightEvent = new PlayerInteractEvent(rightIntruder, Action.RIGHT_CLICK_BLOCK, null, rightHalf, BlockFace.UP);
        listener.onPlayerInteract(rightEvent);

        assertTrue(rightEvent.isCancelled());
        verify(rightIntruder).sendMessage(config.lockedUseMessage());
    }

    @Test
    void authorizedPlayerCanUseEitherDoubleChestHalf() {
        Block leftHalf = world.getBlockAt(10, 64, 0);
        Block rightHalf = world.getBlockAt(11, 64, 0);
        configureDoubleChest(leftHalf, rightHalf, BlockFace.NORTH);
        placeWallSign(leftHalf, BlockFace.NORTH, "[private]", "Owner", "Friend", "");

        Player owner = mockPlayer("Owner");
        PlayerInteractEvent leftEvent = new PlayerInteractEvent(owner, Action.RIGHT_CLICK_BLOCK, null, leftHalf, BlockFace.UP);
        listener.onPlayerInteract(leftEvent);
        assertFalse(leftEvent.isCancelled());

        Player friend = mockPlayer("Friend");
        PlayerInteractEvent rightEvent = new PlayerInteractEvent(friend, Action.RIGHT_CLICK_BLOCK, null, rightHalf, BlockFace.UP);
        listener.onPlayerInteract(rightEvent);
        assertFalse(rightEvent.isCancelled());

        verify(owner, never()).sendMessage(config.lockedUseMessage());
        verify(friend, never()).sendMessage(config.lockedUseMessage());
    }

    @Test
    void ownerRightClickOnManagedSignPrefersGuiOverVanillaSignEditor() {
        Block chest = world.getBlockAt(20, 64, 0);
        chest.setType(Material.CHEST);
        Sign sign = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "", "");

        Player owner = mockPlayer("Owner");
        PlayerInteractEvent event = new PlayerInteractEvent(owner, Action.RIGHT_CLICK_BLOCK, null, sign.getBlock(), BlockFace.UP);

        listener.onPlayerInteract(event);

        assertTrue(event.isCancelled());
        verify(guiService).openFor(owner, sign);
        verify(owner, never()).openSign(any(Sign.class));
        verify(owner, never()).sendMessage(config.lockedUseMessage());
    }

    private SignLockConfig createConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", List.of("CHEST", "BARREL", "SHULKER_BOX"));
        yaml.set("messages.locked-use", "&clocked-use");
        yaml.set("messages.locked-container", "&clocked-container");
        return new SignLockConfig(yaml);
    }

    private Player mockPlayer(String name) {
        Player player = Mockito.mock(Player.class);
        when(player.getName()).thenReturn(name);
        when(player.getUniqueId()).thenReturn(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        when(player.hasPermission(anyString())).thenReturn(false);
        when(player.isSneaking()).thenReturn(false);
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
