package ym.signLock.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LockListenerSignEditTest {

    private ServerMock server;
    private WorldMock world;
    private SignLockConfig config;
    private PlayerIdentityService playerIdentityService;
    private LockListener listener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        config = createConfig();
        playerIdentityService = Mockito.mock(PlayerIdentityService.class);
        listener = new LockListener(new LockService(config, playerIdentityService), playerIdentityService, config);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void ownerEditingPrimaryLockKeepsHeaderAndOwnerButCanChangeAllowedPlayers() {
        Block chest = world.getBlockAt(0, 64, 0);
        chest.setType(Material.CHEST);
        Sign sign = placeManagedWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Alice", "");

        Player owner = mockPlayer("Owner");
        SignChangeEvent event = mockSignChangeEvent(sign.getBlock(), owner,
                "broken-header", "Intruder", "Bob", "Charlie");
        listener.onSignChange(event);

        assertFalse(event.isCancelled());
        assertEquals(config.lockHeader(), event.getLine(0));
        assertEquals("Owner", event.getLine(1));
        assertEquals("Bob", event.getLine(2));
        assertEquals("Charlie", event.getLine(3));
        verify(owner, never()).sendMessage(config.signEditDeniedMessage());
    }

    @Test
    void ownerEditingMoreUsersSignKeepsHeaderButCanChangeAuthorizedLines() {
        Block chest = world.getBlockAt(10, 64, 0);
        chest.setType(Material.CHEST);
        placeManagedWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "", "");
        Sign extension = placeManagedWallSign(chest, BlockFace.SOUTH, "[more users]", "Alice", "", "");

        Player owner = mockPlayer("Owner");
        SignChangeEvent event = mockSignChangeEvent(extension.getBlock(), owner,
                "not-more-users", "Bob", "Charlie", "");
        listener.onSignChange(event);

        assertFalse(event.isCancelled());
        assertEquals(config.moreUsersHeader(), event.getLine(0));
        assertEquals("Bob", event.getLine(1));
        assertEquals("Charlie", event.getLine(2));
        assertEquals("", event.getLine(3));
    }

    @Test
    void nonOwnerEditingManagedSignIsDenied() {
        Block chest = world.getBlockAt(20, 64, 0);
        chest.setType(Material.CHEST);
        Sign sign = placeManagedWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "", "");

        Player intruder = mockPlayer("Intruder");
        SignChangeEvent event = mockSignChangeEvent(sign.getBlock(), intruder,
                "[private]", "Owner", "Mallory", "");
        listener.onSignChange(event);

        assertTrue(event.isCancelled());
        verify(intruder).sendMessage(config.signEditDeniedMessage());
    }

    private SignChangeEvent mockSignChangeEvent(Block signBlock, Player player, String... initialLines) {
        String[] lines = new String[]{"", "", "", ""};
        boolean[] cancelled = new boolean[]{false};
        System.arraycopy(initialLines, 0, lines, 0, Math.min(initialLines.length, lines.length));
        SignChangeEvent event = Mockito.mock(SignChangeEvent.class);
        when(event.getBlock()).thenReturn(signBlock);
        when(event.getPlayer()).thenReturn(player);
        when(event.getLine(anyInt())).thenAnswer(invocation -> lines[invocation.getArgument(0)]);
        Mockito.doAnswer(invocation -> {
            lines[invocation.getArgument(0)] = invocation.getArgument(1);
            applyLinesToBlock(signBlock, lines);
            return null;
        }).when(event).setLine(anyInt(), anyString());
        Mockito.doAnswer(invocation -> {
            cancelled[0] = invocation.getArgument(0);
            return null;
        }).when(event).setCancelled(Mockito.anyBoolean());
        when(event.isCancelled()).thenAnswer(invocation -> cancelled[0]);
        return event;
    }

    private SignLockConfig createConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", List.of("CHEST", "BARREL", "SHULKER_BOX"));
        yaml.set("messages.sign-edit-denied", "&csign-edit-denied");
        return new SignLockConfig(yaml);
    }

    private Player mockPlayer(String name) {
        Player player = Mockito.mock(Player.class);
        when(player.getName()).thenReturn(name);
        when(player.getUniqueId()).thenReturn(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        when(player.hasPermission(anyString())).thenReturn(false);
        return player;
    }

    private Sign placeManagedWallSign(Block target, BlockFace signFace, String... lines) {
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

    private void applyLinesToBlock(Block signBlock, String[] lines) {
        Sign sign = (Sign) signBlock.getState();
        for (int index = 0; index < lines.length; index++) {
            sign.setLine(index, lines[index]);
        }
        sign.update(true, false);
    }
}
