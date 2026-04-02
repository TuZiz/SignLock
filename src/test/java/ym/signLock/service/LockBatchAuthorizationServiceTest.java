package ym.signLock.service;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LockBatchAuthorizationServiceTest {

    private final LockService lockService = Mockito.mock(LockService.class);
    private final LockPlayerNameNormalizer playerNameNormalizer = Mockito.mock(LockPlayerNameNormalizer.class);
    private final Player actor = Mockito.mock(Player.class);
    private final Sign preferredSign = Mockito.mock(Sign.class);
    private final LockService.LockInfo lock = new LockService.LockInfo(
            Mockito.mock(org.bukkit.block.Block.class),
            Mockito.mock(org.bukkit.block.Block.class),
            "Owner",
            Set.of("Existing"),
            LockService.LockType.PRIMARY
    );

    private LockBatchAuthorizationService batchAuthorizationService;

    @BeforeEach
    void setUp() {
        batchAuthorizationService = new LockBatchAuthorizationService(lockService, playerNameNormalizer);
    }

    @Test
    void addPlayersGroupsMixedResultsWithoutRollback() {
        when(lockService.canManage(lock, actor)).thenReturn(true);
        when(playerNameNormalizer.normalize(actor, "Alice")).thenReturn("Alice");
        when(playerNameNormalizer.normalize(actor, "Bob")).thenReturn("Bob");
        when(playerNameNormalizer.normalize(actor, "Carol")).thenReturn("Carol");
        when(playerNameNormalizer.normalize(actor, "Dave")).thenReturn("Dave");
        when(lockService.addPlayerToLock(preferredSign, lock, "Alice")).thenReturn(LockService.AddPlayerResult.ADDED);
        when(lockService.addPlayerToLock(preferredSign, lock, "Bob")).thenReturn(LockService.AddPlayerResult.ADDED_WITH_EXTENSION);
        when(lockService.addPlayerToLock(preferredSign, lock, "Carol")).thenReturn(LockService.AddPlayerResult.ALREADY_AUTHORIZED);
        when(lockService.addPlayerToLock(preferredSign, lock, "Dave")).thenReturn(LockService.AddPlayerResult.NO_SPACE);

        LockBatchAuthorizationService.BatchAddSummary summary =
                batchAuthorizationService.addPlayers(actor, preferredSign, lock, List.of("Alice", "Bob", "Carol", "Dave"));

        assertTrue(summary.permitted());
        assertEquals(List.of("Alice", "Bob"), summary.addedPlayers());
        assertEquals(List.of("Bob"), summary.addedWithExtensionPlayers());
        assertEquals(List.of("Carol"), summary.alreadyAuthorizedPlayers());
        assertEquals(List.of("Dave"), summary.noSpacePlayers());
        assertEquals(2, summary.addedCount());
        assertTrue(summary.extensionCreated());
    }

    @Test
    void removePlayersGroupsMixedResultsWithoutRollback() {
        when(lockService.canManage(lock, actor)).thenReturn(true);
        when(playerNameNormalizer.normalize(actor, "Alice")).thenReturn("Alice");
        when(playerNameNormalizer.normalize(actor, "Missing")).thenReturn("Missing");
        when(playerNameNormalizer.normalize(actor, "Owner")).thenReturn("Owner");
        when(lockService.removePlayerFromLock(preferredSign, lock, "Alice")).thenReturn(LockService.RemovePlayerResult.REMOVED);
        when(lockService.removePlayerFromLock(preferredSign, lock, "Missing")).thenReturn(LockService.RemovePlayerResult.NOT_FOUND);
        when(lockService.removePlayerFromLock(preferredSign, lock, "Owner")).thenReturn(LockService.RemovePlayerResult.OWNER_DENIED);

        LockBatchAuthorizationService.BatchRemoveSummary summary =
                batchAuthorizationService.removePlayers(actor, preferredSign, lock, List.of("Alice", "Missing", "Owner"));

        assertTrue(summary.permitted());
        assertEquals(List.of("Alice"), summary.removedPlayers());
        assertEquals(List.of("Missing"), summary.notFoundPlayers());
        assertEquals(List.of("Owner"), summary.ownerDeniedPlayers());
    }

    @Test
    void batchOperationsDenyNonManagersBeforeAnyWrite() {
        when(lockService.canManage(lock, actor)).thenReturn(false);

        LockBatchAuthorizationService.BatchAddSummary addSummary =
                batchAuthorizationService.addPlayers(actor, preferredSign, lock, List.of("Alice", "Bob"));
        LockBatchAuthorizationService.BatchRemoveSummary removeSummary =
                batchAuthorizationService.removePlayers(actor, preferredSign, lock, List.of("Alice", "Bob"));

        assertFalse(addSummary.permitted());
        assertFalse(removeSummary.permitted());
        verify(lockService, never()).addPlayerToLock(Mockito.any(), Mockito.any(), Mockito.anyString());
        verify(lockService, never()).removePlayerFromLock(Mockito.any(), Mockito.any(), Mockito.anyString());
    }
}
