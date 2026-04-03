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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SignLockCommandInfoCompatibilityTest {

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

        when(player.getTargetBlockExact(6)).thenReturn(targetBlock);
        when(targetBlock.getType()).thenReturn(Material.OAK_WALL_SIGN);
        when(targetBlock.getState()).thenReturn(targetSign);
        when(targetSign.getBlock()).thenReturn(targetBlock);
        when(lockService.findManagedSignLock(targetBlock)).thenReturn(lockInfo);
    }

    @Test
    void infoHidesAuthorizedRosterForDeniedViewersButKeepsSharedSummaryLanguage() {
        LockService.LockDetails details = new LockService.LockDetails(
                "Owner",
                List.of("CurrentName"),
                2,
                new LockService.LockTargetDetails("CHEST", "world", 3, 70, 9, LockService.LockTargetKind.DOUBLE_CHEST)
        );
        when(lockService.describeLock(targetBlock)).thenReturn(details);
        when(lockService.viewerScope(lockInfo, player)).thenReturn(LockService.LockViewerScope.DENIED);

        signLockCommand.onCommand(player, command, "signlock", new String[]{"info"});

        verify(player).sendMessage(config.infoHeaderMessage());
        verify(player).sendMessage(config.infoOwnerMessage("Owner"));
        verify(player).sendMessage(config.infoScopeMessage(config.scopeDeniedLabel()));
        verify(player).sendMessage(config.infoTargetMessage(config.summaryDoubleChestTargetLabel("world", 3, 70, 9)));
        verify(player).sendMessage(config.infoPlayersHiddenMessage());
        verify(player, never()).sendMessage(config.infoPlayersMessage("CurrentName"));
        verify(player).sendMessage(config.infoExtensionsMessage(2));
    }

    private SignLockConfig createConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", List.of("CHEST", "BARREL"));
        yaml.set("messages.info-header", "&6info-header");
        yaml.set("messages.info-owner", "&eowner %owner%");
        yaml.set("messages.info-players", "&eplayers %players%");
        yaml.set("messages.info-players-hidden", "&ehidden");
        yaml.set("messages.info-no-players", "&enobody");
        yaml.set("messages.info-scope", "&escope %scope%");
        yaml.set("messages.info-target", "&etarget %target%");
        yaml.set("messages.info-extensions", "&eextensions %count%");
        yaml.set("messages.scope-denied-label", "denied");
        yaml.set("messages.target-summary-double-chest", "double chest %world% %x% %y% %z%");
        return new SignLockConfig(yaml);
    }
}
