package ym.signLock.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;
import org.mockito.Mockito;
import ym.signLock.config.SignLockConfig;
import ym.signLock.service.LockBatchAuthorizationService;
import ym.signLock.service.LockBatchTargetParser;
import ym.signLock.service.LockPlayerNameNormalizer;
import ym.signLock.service.LockService;
import ym.signLock.service.PlayerIdentityService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LockManagementBatchGuiTest {

    private ServerMock server;
    private WorldMock world;
    private SignLockConfig config;
    private PlayerIdentityService playerIdentityService;
    private LockService lockService;
    private LockManagementGuiService realGuiService;
    private LockManagementGuiService guiService;
    private LockManagementPendingInputStore pendingInputStore;
    private LockManagementGuiActionService actionService;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        config = createConfig();
        playerIdentityService = Mockito.mock(PlayerIdentityService.class);
        lockService = new LockService(config, playerIdentityService);
        realGuiService = new LockManagementGuiService(lockService, config);
        guiService = Mockito.mock(LockManagementGuiService.class);
        LockPlayerNameNormalizer normalizer = new LockPlayerNameNormalizer(playerIdentityService);
        actionService = new LockManagementGuiActionService(
                lockService,
                new LockBatchTargetParser(),
                new LockBatchAuthorizationService(lockService, normalizer),
                normalizer,
                guiService,
                pendingInputStore = new LockManagementPendingInputStore(),
                config
        );
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void playerSlotClickTogglesSelectionAndConfirmRemovesSelectedPlayers() {
        Block chest = world.getBlockAt(0, 64, 0);
        chest.setType(Material.CHEST);
        Sign primary = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Alice", "Bob");
        LockManagementGuiHolder holder = realGuiService.createHolder(primary.getBlock());
        Player owner = mockPlayer("Owner");

        actionService.handleClick(owner, holder, LockManagementGui.PLAYER_SLOTS[0]);
        actionService.handleClick(owner, holder, LockManagementGui.PLAYER_SLOTS[1]);

        assertTrue(holder.isSelected(LockManagementGui.PLAYER_SLOTS[0]));
        assertTrue(holder.isSelected(LockManagementGui.PLAYER_SLOTS[1]));

        actionService.handleClick(owner, holder, LockManagementGui.REMOVE_SELECTED_SLOT);

        Sign updated = (Sign) primary.getBlock().getState();
        assertEquals("", updated.getLine(2));
        assertEquals("", updated.getLine(3));
        verify(owner).sendMessage(contains("批量移除完成"));
        verify(owner).sendMessage(contains("Alice"));
        verify(owner).sendMessage(contains("Bob"));
        verify(guiService).openFor(owner, holder.session());
    }

    @Test
    void inventoryHighlightsSelectedPlayersAndRemoveButtonCount() {
        Block chest = world.getBlockAt(10, 64, 0);
        chest.setType(Material.CHEST);
        Sign primary = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Alice", "");
        LockManagementGuiHolder holder = realGuiService.createHolder(primary.getBlock());

        holder.toggleSelectedPlayer(LockManagementGui.PLAYER_SLOTS[0]);
        var inventory = realGuiService.buildInventory(holder);

        ItemStack selectedPlayer = inventory.getItem(LockManagementGui.PLAYER_SLOTS[0]);
        ItemStack removeButton = inventory.getItem(LockManagementGui.REMOVE_SELECTED_SLOT);
        assertEquals(Material.RED_DYE, selectedPlayer.getType());
        assertEquals(
                ChatColor.stripColor(config.guiRemoveSelectedButtonLabel(1)),
                ChatColor.stripColor(removeButton.getItemMeta().getDisplayName())
        );
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

    private Player mockPlayer(String name) {
        Player player = Mockito.mock(Player.class);
        when(player.getName()).thenReturn(name);
        when(player.getUniqueId()).thenReturn(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        when(player.hasPermission(anyString())).thenReturn(false);
        return player;
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
