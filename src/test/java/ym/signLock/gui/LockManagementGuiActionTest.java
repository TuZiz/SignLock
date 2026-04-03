package ym.signLock.gui;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LockManagementGuiActionTest {

    private ServerMock server;
    private WorldMock world;
    private SignLockConfig config;
    private PlayerIdentityService playerIdentityService;
    private LockService lockService;
    private LockBatchAuthorizationService batchAuthorizationService;
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
        batchAuthorizationService = new LockBatchAuthorizationService(lockService, new LockPlayerNameNormalizer(playerIdentityService));
        realGuiService = new LockManagementGuiService(lockService, config);
        guiService = Mockito.mock(LockManagementGuiService.class);
        pendingInputStore = new LockManagementPendingInputStore();
        actionService = new LockManagementGuiActionService(
                lockService,
                new LockBatchTargetParser(),
                batchAuthorizationService,
                new LockPlayerNameNormalizer(playerIdentityService),
                guiService,
                pendingInputStore,
                config
        );
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void clickingAuthorizedPlayerSlotOnlyTogglesSelection() {
        Block chest = world.getBlockAt(0, 64, 0);
        chest.setType(Material.CHEST);
        Sign primary = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Alice", "");
        LockManagementGuiHolder holder = realGuiService.createHolder(primary.getBlock());
        Player owner = mockPlayer("Owner");

        actionService.handleClick(owner, holder, LockManagementGui.PLAYER_SLOTS[0]);

        Sign unchanged = (Sign) primary.getBlock().getState();
        assertEquals("Alice", unchanged.getLine(2));
        assertTrue(holder.isSelected(LockManagementGui.PLAYER_SLOTS[0]));
        verify(guiService).openFor(owner, holder);
    }

    @Test
    void addButtonStartsPendingInputFlow() {
        Block chest = world.getBlockAt(10, 64, 0);
        chest.setType(Material.CHEST);
        Sign primary = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "", "");
        LockManagementGuiHolder holder = realGuiService.createHolder(primary.getBlock());
        Player owner = mockPlayer("Owner");

        actionService.handleClick(owner, holder, LockManagementGui.ADD_SLOT);

        assertTrue(pendingInputStore.hasPendingAdd(owner.getUniqueId()));
        verify(owner).closeInventory();
        verify(owner).sendMessage(config.guiAddPromptMessage());
    }

    @Test
    void chatInputAddsPlayersUsingSharedBatchNormalization() {
        Block chest = world.getBlockAt(20, 64, 0);
        chest.setType(Material.CHEST);
        Sign primary = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "", "");
        LockManagementGuiHolder holder = realGuiService.createHolder(primary.getBlock());
        Player owner = mockPlayer("Owner");
        when(playerIdentityService.resolveStoredName("oldname")).thenReturn("CurrentName");

        pendingInputStore.beginAdd(owner.getUniqueId(), holder.session());
        actionService.handleChatInput(owner, "oldname Guest");

        Sign updated = (Sign) primary.getBlock().getState();
        assertEquals("CurrentName", updated.getLine(2));
        assertEquals("Guest", updated.getLine(3));
        verify(owner).sendMessage(config.batchAddSummaryMessage(List.of("CurrentName", "Guest"), List.of(), List.of()));
        verify(guiService).openFor(owner, holder.session());
    }

    @Test
    void readOnlyHolderRejectsWriteClicks() {
        Block chest = world.getBlockAt(30, 64, 0);
        chest.setType(Material.CHEST);
        Sign primary = placeWallSign(chest, BlockFace.NORTH, "[private]", "Owner", "Alice", "");
        LockService.LockInfo lock = lockService.findManagedSignLock(primary.getBlock());
        LockService.LockDetails details = lockService.describeLock(primary.getBlock());
        LockManagementGuiHolder holder = new LockManagementGuiHolder(
                LockManagementSession.from(lock),
                LockSummaryView.from(lock, details, LockService.LockViewerScope.ACCESS)
        );
        Player viewer = mockPlayer("Viewer");

        actionService.handleClick(viewer, holder, LockManagementGui.ADD_SLOT);

        assertTrue(!pendingInputStore.hasPendingAdd(viewer.getUniqueId()));
        verify(viewer).sendMessage(config.guiReadOnlyMessage());
        verify(guiService, Mockito.never()).openFor(viewer, holder.session());
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
        yaml.set("messages.add-already-authorized", "&ealready %player%");
        yaml.set("messages.add-list-full", "&clist-full");
        yaml.set("messages.extension-created", "&aextension-created");
        yaml.set("messages.gui-add-prompt", "&eprompt");
        yaml.set("messages.gui-read-only-message", "&eread-only");
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
