package ym.signLock.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LockListenerBreakProtectionTest {

    private ServerMock server;
    private WorldMock world;
    private SignLockConfig config;
    private PlayerIdentityService playerIdentityService;
    private LockListener listener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        config = createConfig(true);
        playerIdentityService = Mockito.mock(PlayerIdentityService.class);
        listener = new LockListener(new LockService(config, playerIdentityService), playerIdentityService, config);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void unauthorizedPlayerCannotBreakProtectedContainerBody() {
        Block barrel = protectedBarrel(0, 64, 0, "Owner", "Friend");
        Player intruder = mockPlayer("Intruder", false);
        BlockBreakEvent event = mockBreakEvent(barrel, intruder);

        listener.onBlockBreak(event);

        verify(event).setCancelled(true);
        verify(intruder).sendMessage(config.protectedBlockMessage());
    }

    @Test
    void authorizedPlayerCanAccessButCannotBreakProtectedContainerBody() {
        Block barrel = protectedBarrel(10, 64, 0, "Owner", "Friend");
        Player friend = mockPlayer("Friend", false);
        BlockBreakEvent event = mockBreakEvent(barrel, friend);

        listener.onBlockBreak(event);

        verify(event).setCancelled(true);
        verify(friend).sendMessage(config.protectedBlockMessage());
    }

    @Test
    void unauthorizedPlayerCannotBreakPrimaryOrExtensionSign() {
        Block barrel = protectedBarrel(20, 64, 0, "Owner", "Friend");
        Sign primary = managedWallSign(barrel, BlockFace.NORTH, "[private]", "Owner", "Friend", "");
        Sign extension = managedWallSign(barrel, BlockFace.SOUTH, "[more users]", "Guest", "", "");
        Player intruder = mockPlayer("Intruder", false);

        BlockBreakEvent primaryEvent = mockBreakEvent(primary.getBlock(), intruder);
        listener.onBlockBreak(primaryEvent);
        verify(primaryEvent).setCancelled(true);
        verify(intruder).sendMessage(config.protectedSignMessage());

        BlockBreakEvent extensionEvent = mockBreakEvent(extension.getBlock(), intruder);
        listener.onBlockBreak(extensionEvent);
        verify(extensionEvent).setCancelled(true);
        verify(intruder, times(2)).sendMessage(config.protectedSignMessage());
    }

    @Test
    void authorizedPlayerCannotBreakAnotherOwnersManagedSigns() {
        Block barrel = protectedBarrel(30, 64, 0, "Owner", "Friend");
        Sign primary = managedWallSign(barrel, BlockFace.NORTH, "[private]", "Owner", "Friend", "");
        Sign extension = managedWallSign(barrel, BlockFace.SOUTH, "[more users]", "Guest", "", "");
        Player friend = mockPlayer("Friend", false);

        BlockBreakEvent primaryEvent = mockBreakEvent(primary.getBlock(), friend);
        listener.onBlockBreak(primaryEvent);
        verify(primaryEvent).setCancelled(true);
        verify(friend).sendMessage(config.protectedSignMessage());

        BlockBreakEvent extensionEvent = mockBreakEvent(extension.getBlock(), friend);
        listener.onBlockBreak(extensionEvent);
        verify(extensionEvent).setCancelled(true);
        verify(friend, times(2)).sendMessage(config.protectedSignMessage());
    }

    @Test
    void ownerCanBreakProtectedBlockAndManagedSigns() {
        Block barrel = protectedBarrel(40, 64, 0, "Owner", "Friend");
        Sign primary = managedWallSign(barrel, BlockFace.NORTH, "[private]", "Owner", "Friend", "");
        Player owner = mockPlayer("Owner", false);

        BlockBreakEvent blockEvent = mockBreakEvent(barrel, owner);
        listener.onBlockBreak(blockEvent);
        verify(blockEvent, never()).setCancelled(true);
        verify(owner, never()).sendMessage(config.protectedBlockMessage());

        BlockBreakEvent signEvent = mockBreakEvent(primary.getBlock(), owner);
        listener.onBlockBreak(signEvent);
        verify(signEvent, never()).setCancelled(true);
        verify(owner, never()).sendMessage(config.protectedSignMessage());
    }

    @Test
    void adminBypassCanBreakProtectedBlockAndManagedSigns() {
        Block barrel = protectedBarrel(50, 64, 0, "Owner", "Friend");
        Sign extension = managedWallSign(barrel, BlockFace.SOUTH, "[more users]", "Guest", "", "");
        Player admin = mockPlayer("Admin", true);

        BlockBreakEvent blockEvent = mockBreakEvent(barrel, admin);
        listener.onBlockBreak(blockEvent);
        verify(blockEvent, never()).setCancelled(true);
        verify(admin, never()).sendMessage(config.protectedBlockMessage());

        BlockBreakEvent signEvent = mockBreakEvent(extension.getBlock(), admin);
        listener.onBlockBreak(signEvent);
        verify(signEvent, never()).setCancelled(true);
        verify(admin, never()).sendMessage(config.protectedSignMessage());
    }

    private SignLockConfig createConfig(boolean adminBypass) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.admin-bypass", adminBypass);
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", List.of("BARREL"));
        yaml.set("messages.protected-block", "&cprotected-block");
        yaml.set("messages.protected-sign", "&cprotected-sign");
        return new SignLockConfig(yaml);
    }

    private Player mockPlayer(String name, boolean admin) {
        Player player = Mockito.mock(Player.class);
        when(player.getName()).thenReturn(name);
        when(player.getUniqueId()).thenReturn(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        when(player.hasPermission(anyString())).thenAnswer(invocation -> admin && "signlock.admin".equals(invocation.getArgument(0)));
        return player;
    }

    private BlockBreakEvent mockBreakEvent(Block block, Player player) {
        BlockBreakEvent event = Mockito.mock(BlockBreakEvent.class);
        when(event.getBlock()).thenReturn(block);
        when(event.getPlayer()).thenReturn(player);
        return event;
    }

    private Block protectedBarrel(int x, int y, int z, String owner, String allowedPlayer) {
        Block barrel = world.getBlockAt(x, y, z);
        barrel.setType(Material.BARREL);
        managedWallSign(barrel, BlockFace.NORTH, "[private]", owner, allowedPlayer, "");
        return barrel;
    }

    private Sign managedWallSign(Block target, BlockFace signFace, String... lines) {
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
