package ym.signLock.command;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ym.signLock.SignLock;
import ym.signLock.config.SignLockConfig;
import ym.signLock.service.LockService;
import ym.signLock.service.PlayerIdentityService;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SignLockCommandBatchCompatibilityTest {

    private final SignLock plugin = Mockito.mock(SignLock.class);
    private final LockService lockService = Mockito.mock(LockService.class);
    private final PlayerIdentityService playerIdentityService = Mockito.mock(PlayerIdentityService.class);
    private final Player player = Mockito.mock(Player.class);
    private final Sign targetSign = Mockito.mock(Sign.class);
    private final Block targetBlock = Mockito.mock(Block.class);
    private final Command command = Mockito.mock(Command.class);

    private SignLockCommand signLockCommand;
    private SignLockConfig config;
    private LockService.LockInfo lockInfo;

    @BeforeEach
    void setUp() {
        config = createConfig();
        signLockCommand = new SignLockCommand(plugin);
        lockInfo = new LockService.LockInfo(targetBlock, targetBlock, "Owner", Set.of("Existing"), LockService.LockType.PRIMARY);

        when(plugin.getSignLockConfig()).thenReturn(config);
        when(plugin.getLockService()).thenReturn(lockService);
        when(plugin.getPlayerIdentityService()).thenReturn(playerIdentityService);

        when(player.hasPermission("signlock.add")).thenReturn(true);
        when(player.getTargetBlockExact(6)).thenReturn(targetBlock);
        when(targetBlock.getType()).thenReturn(Material.OAK_WALL_SIGN);
        when(targetBlock.getState()).thenReturn(targetSign);
        when(targetSign.getBlock()).thenReturn(targetBlock);

        when(lockService.findManagedSignLock(targetBlock)).thenReturn(lockInfo);
        when(lockService.canManage(lockInfo, player)).thenReturn(true);
    }

    @Test
    void addAcceptsMultipleTargetsAndReturnsAggregatedFeedback() {
        when(playerIdentityService.resolveStoredName("Alice")).thenReturn("Alice");
        when(playerIdentityService.resolveStoredName("Bob")).thenReturn("Bob");
        when(playerIdentityService.resolveStoredName("Charlie")).thenReturn("Charlie");
        when(lockService.addPlayerToLock(targetSign, lockInfo, "Alice")).thenReturn(LockService.AddPlayerResult.ADDED);
        when(lockService.addPlayerToLock(targetSign, lockInfo, "Bob")).thenReturn(LockService.AddPlayerResult.ALREADY_AUTHORIZED);
        when(lockService.addPlayerToLock(targetSign, lockInfo, "Charlie")).thenReturn(LockService.AddPlayerResult.NO_SPACE);

        signLockCommand.onCommand(player, command, "signlock", new String[]{"add", "Alice", "Bob,Charlie"});

        verify(lockService).addPlayerToLock(targetSign, lockInfo, "Alice");
        verify(lockService).addPlayerToLock(targetSign, lockInfo, "Bob");
        verify(lockService).addPlayerToLock(targetSign, lockInfo, "Charlie");
        verify(player).sendMessage(config.batchAddSummaryMessage(List.of("Alice"), List.of("Bob"), List.of("Charlie")));
    }

    @Test
    void removeAcceptsMultipleTargetsAndReturnsAggregatedFeedback() {
        when(playerIdentityService.resolveStoredName("Alice")).thenReturn("Alice");
        when(playerIdentityService.resolveStoredName("Missing")).thenReturn("Missing");
        when(playerIdentityService.resolveStoredName("Owner")).thenReturn("Owner");
        when(lockService.removePlayerFromLock(targetSign, lockInfo, "Alice")).thenReturn(LockService.RemovePlayerResult.REMOVED);
        when(lockService.removePlayerFromLock(targetSign, lockInfo, "Missing")).thenReturn(LockService.RemovePlayerResult.NOT_FOUND);
        when(lockService.removePlayerFromLock(targetSign, lockInfo, "Owner")).thenReturn(LockService.RemovePlayerResult.OWNER_DENIED);

        signLockCommand.onCommand(player, command, "signlock", new String[]{"remove", "Alice", "Missing,Owner"});

        verify(lockService).removePlayerFromLock(targetSign, lockInfo, "Alice");
        verify(lockService).removePlayerFromLock(targetSign, lockInfo, "Missing");
        verify(lockService).removePlayerFromLock(targetSign, lockInfo, "Owner");
        verify(player).sendMessage(config.batchRemoveSummaryMessage(List.of("Alice"), List.of("Missing"), List.of("Owner")));
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
}
