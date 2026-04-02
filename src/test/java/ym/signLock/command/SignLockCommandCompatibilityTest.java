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

class SignLockCommandCompatibilityTest {

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
        lockInfo = new LockService.LockInfo(targetBlock, targetBlock, "Owner", Set.of("CurrentName"), LockService.LockType.PRIMARY);

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
    void addStillWorksAfterSharedNormalizerExtraction() {
        when(playerIdentityService.resolveStoredName("oldname")).thenReturn("CurrentName");
        when(lockService.addPlayerToLock(targetSign, lockInfo, "CurrentName")).thenReturn(LockService.AddPlayerResult.ADDED);

        signLockCommand.onCommand(player, command, "signlock", new String[]{"add", "oldname"});

        verify(lockService).addPlayerToLock(targetSign, lockInfo, "CurrentName");
        verify(player).sendMessage(config.addSuccessMessage("CurrentName"));
    }

    @Test
    void removeStillWorksAfterSharedNormalizerExtraction() {
        when(playerIdentityService.resolveStoredName("oldname")).thenReturn("CurrentName");
        when(lockService.removePlayerFromLock(targetSign, lockInfo, "CurrentName"))
                .thenReturn(LockService.RemovePlayerResult.REMOVED);

        signLockCommand.onCommand(player, command, "signlock", new String[]{"remove", "oldname"});

        verify(lockService).removePlayerFromLock(targetSign, lockInfo, "CurrentName");
        verify(player).sendMessage(config.removeSuccessMessage("CurrentName"));
    }

    @Test
    void infoStillShowsLockSummary() {
        LockService.LockDetails details = new LockService.LockDetails("Owner", List.of("CurrentName"), 1);
        when(lockService.describeLock(targetBlock)).thenReturn(details);

        signLockCommand.onCommand(player, command, "signlock", new String[]{"info"});

        verify(lockService).findManagedSignLock(targetBlock);
        verify(lockService).describeLock(targetBlock);
        verify(player).sendMessage(config.infoHeaderMessage());
        verify(player).sendMessage(config.infoOwnerMessage("Owner"));
        verify(player).sendMessage(config.infoPlayersMessage("CurrentName"));
        verify(player).sendMessage(config.infoExtensionsMessage(1));
    }

    private SignLockConfig createConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", List.of("CHEST", "BARREL"));
        yaml.set("messages.add-success", "&aadd-success %player%");
        yaml.set("messages.remove-success", "&aremove-success %player%");
        yaml.set("messages.info-header", "&6info-header");
        yaml.set("messages.info-owner", "&eowner %owner%");
        yaml.set("messages.info-players", "&eplayers %players%");
        yaml.set("messages.info-no-players", "&enobody");
        yaml.set("messages.info-extensions", "&eextensions %count%");
        return new SignLockConfig(yaml);
    }
}
