package ym.signLock.gui;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.mockito.Mockito;
import ym.signLock.config.SignLockConfig;
import ym.signLock.listener.LockGuiListener;
import ym.signLock.service.LockService;
import ym.signLock.service.PlayerIdentityService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LockGuiListenerClickSafetyTest {

    private ServerMock server;
    private WorldMock world;
    private LockManagementGuiHolder holder;
    private LockManagementGuiActionService actionService;
    private List<Runnable> queuedTasks;
    private LockGuiListener listener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        SignLockConfig config = createConfig();
        PlayerIdentityService playerIdentityService = Mockito.mock(PlayerIdentityService.class);
        LockManagementGuiService guiService = new LockManagementGuiService(new LockService(config, playerIdentityService), config);
        Block chest = world.getBlockAt(0, 64, 0);
        chest.setType(Material.CHEST);
        Sign sign = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Alice", "");
        holder = guiService.createHolder(sign.getBlock());
        guiService.buildInventory(holder);

        actionService = Mockito.mock(LockManagementGuiActionService.class);
        queuedTasks = new ArrayList<>();
        listener = new LockGuiListener(queuedTasks::add, actionService);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void topInventoryClicksAreCancelledAndDeferredToNextTick() {
        Player player = Mockito.mock(Player.class);
        InventoryView view = Mockito.mock(InventoryView.class);
        InventoryClickEvent event = Mockito.mock(InventoryClickEvent.class);

        when(view.getTopInventory()).thenReturn(holder.getInventory());
        when(event.getView()).thenReturn(view);
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getRawSlot()).thenReturn(LockManagementGui.ADD_SLOT);
        when(event.isRightClick()).thenReturn(false);
        when(event.isShiftClick()).thenReturn(false);

        listener.onInventoryClick(event);

        verify(event).setCancelled(true);
        verify(actionService, never()).handleClick(player, holder, LockManagementGui.ADD_SLOT, false, false);
        assertEquals(1, queuedTasks.size());

        queuedTasks.get(0).run();
        verify(actionService).handleClick(player, holder, LockManagementGui.ADD_SLOT, false, false);
    }

    @Test
    void playerInventoryClicksAreCancelledButDoNotDispatchActions() {
        Player player = Mockito.mock(Player.class);
        InventoryView view = Mockito.mock(InventoryView.class);
        InventoryClickEvent event = Mockito.mock(InventoryClickEvent.class);

        when(view.getTopInventory()).thenReturn(holder.getInventory());
        when(event.getView()).thenReturn(view);
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getRawSlot()).thenReturn(holder.getInventory().getSize() + 5);

        listener.onInventoryClick(event);

        verify(event).setCancelled(true);
        verify(actionService, never()).handleClick(
                Mockito.any(Player.class),
                Mockito.any(LockManagementGuiHolder.class),
                anyInt(),
                anyBoolean(),
                anyBoolean()
        );
        assertEquals(0, queuedTasks.size());
    }

    @Test
    void dragsIntoTopInventoryAreCancelled() {
        InventoryView view = Mockito.mock(InventoryView.class);
        InventoryDragEvent event = Mockito.mock(InventoryDragEvent.class);

        when(view.getTopInventory()).thenReturn(holder.getInventory());
        when(event.getView()).thenReturn(view);
        when(event.getRawSlots()).thenReturn(Set.of(0, holder.getInventory().getSize() + 1));

        listener.onInventoryDrag(event);

        verify(event).setCancelled(true);
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
