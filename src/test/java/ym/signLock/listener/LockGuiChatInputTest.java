package ym.signLock.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ym.signLock.gui.LockManagementGuiActionService;
import ym.signLock.gui.LockManagementPendingInputStore;
import ym.signLock.gui.LockManagementSession;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LockGuiChatInputTest {

    @Test
    void asyncChatIsCancelledAndHandedBackOnNextTick() {
        LockManagementPendingInputStore pendingInputStore = new LockManagementPendingInputStore();
        LockManagementGuiActionService actionService = Mockito.mock(LockManagementGuiActionService.class);
        List<Runnable> queuedTasks = new ArrayList<>();
        LockGuiChatInputListener listener = new LockGuiChatInputListener(queuedTasks::add, pendingInputStore, actionService);

        Player player = Mockito.mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(playerId);

        pendingInputStore.beginAdd(playerId, new LockManagementSession(playerId, 0, 64, 0, new Location(null, 0, 64, 0)));

        AsyncPlayerChatEvent event = Mockito.mock(AsyncPlayerChatEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getMessage()).thenReturn("Guest");

        listener.onAsyncPlayerChat(event);

        verify(event).setCancelled(true);
        verify(actionService, never()).handleChatInput(player, "Guest");
        assertEquals(1, queuedTasks.size());

        queuedTasks.get(0).run();
        verify(actionService).handleChatInput(player, "Guest");
    }

    @Test
    void unrelatedChatPassesThrough() {
        LockManagementPendingInputStore pendingInputStore = new LockManagementPendingInputStore();
        LockManagementGuiActionService actionService = Mockito.mock(LockManagementGuiActionService.class);
        LockGuiChatInputListener listener = new LockGuiChatInputListener(task -> {
        }, pendingInputStore, actionService);

        Player player = Mockito.mock(Player.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());

        AsyncPlayerChatEvent event = Mockito.mock(AsyncPlayerChatEvent.class);
        when(event.getPlayer()).thenReturn(player);

        listener.onAsyncPlayerChat(event);

        verify(event, never()).setCancelled(true);
        verify(actionService, never()).handleChatInput(Mockito.any(Player.class), Mockito.anyString());
    }
}
