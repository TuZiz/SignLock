package ym.signLock.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
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

class LockListenerPlacementTest {

    private ServerMock server;
    private WorldMock world;
    private SignLockConfig config;
    private PlayerIdentityService playerIdentityService;
    private LockService lockService;
    private LockListener listener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        config = createConfig(2);
        playerIdentityService = Mockito.mock(PlayerIdentityService.class);
        lockService = new LockService(config, playerIdentityService);
        listener = new LockListener(lockService, playerIdentityService, config);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void primaryLockPlacementOnEitherDoubleChestHalfResolvesToSharedTarget() {
        Block leftHalf = world.getBlockAt(0, 64, 0);
        Block rightHalf = world.getBlockAt(1, 64, 0);
        configureDoubleChest(leftHalf, rightHalf, BlockFace.NORTH);

        Player owner = mockPlayer("Owner");
        SignChangeEvent leftEvent = mockSignChangeEvent(placeWallSignBlock(leftHalf, BlockFace.NORTH), owner,
                "[private]", "", "", "");
        listener.onSignChange(leftEvent);

        LockService.LockInfo leftLock = lockService.findLock(leftHalf);
        LockService.LockInfo rightLock = lockService.findLock(rightHalf);

        assertNotCancelled(leftEvent);
        assertEquals(config.lockHeader(), leftEvent.getLine(0));
        assertEquals("Owner", leftEvent.getLine(1));
        assertEquals(coords(leftLock.targetBlock()), coords(rightLock.targetBlock()));

        Block leftTarget = leftLock.targetBlock();
        Block expectedPlacement = lockService.findPlacementTarget(placeWallSignBlock(rightHalf, BlockFace.NORTH));
        assertEquals(coords(leftTarget), coords(expectedPlacement));
        verify(owner).sendMessage(config.lockCreatedMessage("chest"));
        verify(owner).sendMessage(config.lockUsageHintMessage());
    }

    @Test
    void moreUsersSignCanBeAttachedFromOtherHalfByOwnerOnly() {
        Block leftHalf = world.getBlockAt(10, 64, 0);
        Block rightHalf = world.getBlockAt(11, 64, 0);
        configureDoubleChest(leftHalf, rightHalf, BlockFace.NORTH);
        placeManagedWallSign(leftHalf, BlockFace.NORTH, "[private]", "Owner", "", "");

        Player owner = mockPlayer("Owner");
        SignChangeEvent ownerEvent = mockSignChangeEvent(placeWallSignBlock(rightHalf, BlockFace.NORTH), owner,
                "[more users]", "Guest", "", "");
        listener.onSignChange(ownerEvent);

        LockService.LockInfo lock = lockService.findLock(rightHalf);
        assertNotCancelled(ownerEvent);
        assertEquals(config.moreUsersHeader(), ownerEvent.getLine(0));
        assertEquals("", ownerEvent.getLine(1));
        assertEquals("", ownerEvent.getLine(2));
        assertEquals("", ownerEvent.getLine(3));
        assertTrue(lockService.canCreateMoreUsersSign(leftHalf));
        assertEquals(coords(lock.targetBlock()), coords(lockService.findPlacementTarget(ownerEvent.getBlock())));
        verify(owner).sendMessage(config.extraUsersAttachedMessage());

        Player intruder = mockPlayer("Intruder");
        SignChangeEvent intruderEvent = mockSignChangeEvent(placeWallSignBlock(rightHalf, BlockFace.SOUTH), intruder,
                "[more users]", "", "", "");
        listener.onSignChange(intruderEvent);

        assertTrue(intruderEvent.isCancelled());
        verify(intruder).sendMessage(config.ownerOnlyMoreUsersMessage());
    }

    @Test
    void moreUsersCapacityUsesCanonicalTargetRatherThanClickedHalf() {
        config = createConfig(1);
        lockService = new LockService(config, playerIdentityService);
        listener = new LockListener(lockService, playerIdentityService, config);

        Block leftHalf = world.getBlockAt(20, 64, 0);
        Block rightHalf = world.getBlockAt(21, 64, 0);
        configureDoubleChest(leftHalf, rightHalf, BlockFace.NORTH);
        placeManagedWallSign(leftHalf, BlockFace.NORTH, "[private]", "Owner", "", "");
        placeManagedWallSign(rightHalf, BlockFace.NORTH, "[more users]", "Alice", "", "");

        Player owner = mockPlayer("Owner");
        SignChangeEvent event = mockSignChangeEvent(placeWallSignBlock(leftHalf, BlockFace.SOUTH), owner,
                "[more users]", "", "", "");
        listener.onSignChange(event);

        assertTrue(event.isCancelled());
        verify(owner).sendMessage(config.addListFullMessage());
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

    private SignLockConfig createConfig(int maxMoreUsersSigns) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.max-more-user-signs", maxMoreUsersSigns);
        yaml.set("protection.extension-placement-order", List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", List.of("CHEST", "BARREL", "SHULKER_BOX"));
        yaml.set("messages.lock-created", "&alock-created {target}");
        yaml.set("messages.lock-usage-hint", "&7hint");
        yaml.set("messages.extra-users-attached", "&aextra-users-attached");
        yaml.set("messages.owner-only-more-users", "&cowner-only-more-users");
        yaml.set("messages.add-list-full", "&cadd-list-full");
        yaml.set("messages.already-protected", "&calready-protected");
        yaml.set("messages.invalid-lock-placement", "&cinvalid-lock-placement");
        yaml.set("messages.invalid-more-users-placement", "&cinvalid-more-users-placement");
        yaml.set("messages.missing-primary-lock", "&cmissing-primary-lock");
        return new SignLockConfig(yaml);
    }

    private Player mockPlayer(String name) {
        Player player = Mockito.mock(Player.class);
        when(player.getName()).thenReturn(name);
        when(player.getUniqueId()).thenReturn(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
        when(player.hasPermission(anyString())).thenReturn(false);
        return player;
    }

    private void configureDoubleChest(Block leftHalf, Block rightHalf, BlockFace facing) {
        leftHalf.setType(Material.CHEST);
        rightHalf.setType(Material.CHEST);

        Chest leftData = (Chest) Material.CHEST.createBlockData();
        leftData.setFacing(facing);
        leftData.setType(Chest.Type.LEFT);
        leftHalf.setBlockData(leftData, false);

        Chest rightData = (Chest) Material.CHEST.createBlockData();
        rightData.setFacing(facing);
        rightData.setType(Chest.Type.RIGHT);
        rightHalf.setBlockData(rightData, false);
    }

    private Sign placeManagedWallSign(Block target, BlockFace signFace, String... lines) {
        Block signBlock = placeWallSignBlock(target, signFace);
        Sign sign = (Sign) signBlock.getState();
        for (int index = 0; index < lines.length; index++) {
            sign.setLine(index, lines[index]);
        }
        sign.update(true, false);
        return (Sign) signBlock.getState();
    }

    private Block placeWallSignBlock(Block target, BlockFace signFace) {
        Block signBlock = target.getRelative(signFace);
        signBlock.setType(Material.OAK_WALL_SIGN);
        Directional directional = (Directional) signBlock.getBlockData();
        directional.setFacing(signFace);
        signBlock.setBlockData(directional, false);
        return signBlock;
    }

    private void applyLinesToBlock(Block signBlock, String[] lines) {
        Sign sign = (Sign) signBlock.getState();
        for (int index = 0; index < lines.length; index++) {
            sign.setLine(index, lines[index]);
        }
        sign.update(true, false);
    }

    private void assertNotCancelled(SignChangeEvent event) {
        assertFalse(event.isCancelled());
        verify(event, never()).setCancelled(true);
    }

    private String coords(Block block) {
        return block.getX() + "," + block.getY() + "," + block.getZ();
    }
}
