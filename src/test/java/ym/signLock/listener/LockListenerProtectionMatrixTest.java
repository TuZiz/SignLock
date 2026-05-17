package ym.signLock.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LockListenerProtectionMatrixTest {

    private ServerMock server;
    private WorldMock world;
    private LockListener listener;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
        SignLockConfig config = createConfig();
        PlayerIdentityService playerIdentityService = Mockito.mock(PlayerIdentityService.class);
        listener = new LockListener(new LockService(config, playerIdentityService), playerIdentityService, config);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void inventoryMoveCancelsForProtectedSourceAndDestination() {
        Block protectedSource = protectedBarrel(0, 64, 0, "Owner");
        Block protectedDestination = protectedBarrel(10, 64, 0, "Owner");
        Block plainBarrel = world.getBlockAt(20, 64, 0);
        plainBarrel.setType(Material.BARREL);

        InventoryMoveItemEvent sourceEvent = mockInventoryMoveEvent((Container) protectedSource.getState(), (Container) plainBarrel.getState());
        listener.onInventoryMove(sourceEvent);
        verify(sourceEvent).setCancelled(true);

        InventoryMoveItemEvent destinationEvent = mockInventoryMoveEvent((Container) plainBarrel.getState(), (Container) protectedDestination.getState());
        listener.onInventoryMove(destinationEvent);
        verify(destinationEvent).setCancelled(true);
    }

    @Test
    void inventoryMoveCancelsEvenWhenBothEndsAreLockedBySameOwner() {
        Block protectedBarrel = protectedBarrel(24, 64, 0, "Owner");
        Block lockedHopper = lockedHopper(25, 64, 0, "Owner");

        InventoryMoveItemEvent pushIntoBarrel = mockInventoryMoveEvent(
                (Container) lockedHopper.getState(),
                (Container) protectedBarrel.getState()
        );
        listener.onInventoryMove(pushIntoBarrel);
        verify(pushIntoBarrel).setCancelled(true);

        InventoryMoveItemEvent pullFromBarrel = mockInventoryMoveEvent(
                (Container) protectedBarrel.getState(),
                (Container) lockedHopper.getState()
        );
        listener.onInventoryMove(pullFromBarrel);
        verify(pullFromBarrel).setCancelled(true);
    }

    @Test
    void inventoryMoveCancelsWhenLockedAutomationOwnersDiffer() {
        Block protectedBarrel = protectedBarrel(28, 64, 0, "Owner");
        Block lockedHopper = lockedHopper(29, 64, 0, "Intruder");

        InventoryMoveItemEvent event = mockInventoryMoveEvent(
                (Container) lockedHopper.getState(),
                (Container) protectedBarrel.getState()
        );

        listener.onInventoryMove(event);

        verify(event).setCancelled(true);
    }

    @Test
    void explosionsFilterProtectedTargetsAndManagedSigns() {
        Block protectedBarrel = protectedBarrel(30, 64, 0, "Owner");
        Sign primary = managedWallSign(protectedBarrel, BlockFace.NORTH, "[private]", "Owner", "", "");
        Sign extension = managedWallSign(protectedBarrel, BlockFace.SOUTH, "[more users]", "Guest", "", "");
        Block plainBlock = world.getBlockAt(31, 64, 0);
        plainBlock.setType(Material.STONE);

        List<Block> blockExplosionBlocks = new ArrayList<>(List.of(protectedBarrel, primary.getBlock(), extension.getBlock(), plainBlock));
        BlockExplodeEvent blockExplodeEvent = Mockito.mock(BlockExplodeEvent.class);
        when(blockExplodeEvent.blockList()).thenReturn(blockExplosionBlocks);
        listener.onBlockExplode(blockExplodeEvent);
        assertEquals(List.of(plainBlock), blockExplosionBlocks);

        List<Block> entityExplosionBlocks = new ArrayList<>(List.of(primary.getBlock(), plainBlock));
        EntityExplodeEvent entityExplodeEvent = Mockito.mock(EntityExplodeEvent.class);
        when(entityExplodeEvent.blockList()).thenReturn(entityExplosionBlocks);
        when(entityExplodeEvent.getEntity()).thenReturn(Mockito.mock(Entity.class));
        listener.onEntityExplode(entityExplodeEvent);
        assertEquals(List.of(plainBlock), entityExplosionBlocks);
    }

    @Test
    void fluidFlowCancelsBothIntoAndOutOfProtectedStructure() {
        Block protectedBarrel = protectedBarrel(40, 64, 0, "Owner");
        Block water = world.getBlockAt(39, 64, 0);
        water.setType(Material.WATER);
        Block adjacentAir = world.getBlockAt(41, 64, 0);

        BlockFromToEvent intoProtected = Mockito.mock(BlockFromToEvent.class);
        when(intoProtected.getBlock()).thenReturn(water);
        when(intoProtected.getToBlock()).thenReturn(protectedBarrel);
        listener.onFluidFlow(intoProtected);
        verify(intoProtected).setCancelled(true);

        BlockFromToEvent outOfProtected = Mockito.mock(BlockFromToEvent.class);
        when(outOfProtected.getBlock()).thenReturn(protectedBarrel);
        when(outOfProtected.getToBlock()).thenReturn(adjacentAir);
        listener.onFluidFlow(outOfProtected);
        verify(outOfProtected).setCancelled(true);
    }

    @Test
    void fireCannotBurnOrIgniteProtectedStructures() {
        Block protectedBarrel = protectedBarrel(45, 64, 0, "Owner");
        Sign managedSign = managedWallSign(protectedBarrel, BlockFace.SOUTH, "[more users]", "Guest", "", "");
        Block plainBlock = world.getBlockAt(46, 64, 0);
        plainBlock.setType(Material.OAK_PLANKS);

        BlockBurnEvent protectedBurn = Mockito.mock(BlockBurnEvent.class);
        when(protectedBurn.getBlock()).thenReturn(protectedBarrel);
        listener.onBlockBurn(protectedBurn);
        verify(protectedBurn).setCancelled(true);

        BlockBurnEvent signBurn = Mockito.mock(BlockBurnEvent.class);
        when(signBurn.getBlock()).thenReturn(managedSign.getBlock());
        listener.onBlockBurn(signBurn);
        verify(signBurn).setCancelled(true);

        BlockIgniteEvent signIgnite = Mockito.mock(BlockIgniteEvent.class);
        when(signIgnite.getBlock()).thenReturn(managedSign.getBlock());
        listener.onBlockIgnite(signIgnite);
        verify(signIgnite).setCancelled(true);

        BlockBurnEvent plainBurn = Mockito.mock(BlockBurnEvent.class);
        when(plainBurn.getBlock()).thenReturn(plainBlock);
        listener.onBlockBurn(plainBurn);
        verify(plainBurn, never()).setCancelled(true);
    }

    @Test
    void pistonMovementCancelsWhenMovedBlocksContainProtectedTargetsOrManagedSigns() {
        Block protectedBarrel = protectedBarrel(50, 64, 0, "Owner");
        Sign extension = managedWallSign(protectedBarrel, BlockFace.SOUTH, "[more users]", "Guest", "", "");
        Block plainBlock = world.getBlockAt(49, 64, 0);
        plainBlock.setType(Material.STONE);

        BlockPistonExtendEvent extendEvent = Mockito.mock(BlockPistonExtendEvent.class);
        when(extendEvent.getBlocks()).thenReturn(List.of(plainBlock, protectedBarrel));
        listener.onPistonExtend(extendEvent);
        verify(extendEvent).setCancelled(true);

        BlockPistonRetractEvent retractEvent = Mockito.mock(BlockPistonRetractEvent.class);
        when(retractEvent.getBlocks()).thenReturn(List.of(extension.getBlock()));
        listener.onPistonRetract(retractEvent);
        verify(retractEvent).setCancelled(true);
    }

    @Test
    void pistonDestinationCollisionIntoProtectedStructureCancelsEvent() {
        Block protectedBarrel = protectedBarrel(60, 64, 0, "Owner");
        Block movingBlock = world.getBlockAt(59, 64, 0);
        movingBlock.setType(Material.STONE);
        BlockPistonExtendEvent extendEvent = Mockito.mock(BlockPistonExtendEvent.class);
        when(extendEvent.getBlocks()).thenReturn(List.of(movingBlock));
        when(extendEvent.getDirection()).thenReturn(BlockFace.EAST);

        listener.onPistonExtend(extendEvent);

        verify(extendEvent).setCancelled(true);
        assertFalse(List.of(movingBlock).contains(protectedBarrel));
    }

    @Test
    void sharedTargetInventoryMoveStillCancelsWhenDoubleChestOtherHalfIsUsed() {
        Block leftHalf = world.getBlockAt(70, 64, 0);
        Block rightHalf = world.getBlockAt(71, 64, 0);
        configureDoubleChest(leftHalf, rightHalf, BlockFace.NORTH);
        managedWallSign(leftHalf, BlockFace.NORTH, "[private]", "Owner", "", "");

        InventoryHolder sourceHolder = mockDoubleChestHolder(rightHalf);
        Block plainBarrel = world.getBlockAt(72, 64, 0);
        plainBarrel.setType(Material.BARREL);

        Inventory source = Mockito.mock(Inventory.class);
        when(source.getHolder()).thenReturn(sourceHolder);
        Inventory destination = Mockito.mock(Inventory.class);
        when(destination.getHolder()).thenReturn((Container) plainBarrel.getState());
        InventoryMoveItemEvent event = Mockito.mock(InventoryMoveItemEvent.class);
        when(event.getSource()).thenReturn(source);
        when(event.getDestination()).thenReturn(destination);
        when(event.getItem()).thenReturn(new ItemStack(Material.STONE));

        listener.onInventoryMove(event);

        verify(event).setCancelled(true);
    }

    private SignLockConfig createConfig() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("signs.lock-header", "[private]");
        yaml.set("signs.more-users-header", "[more users]");
        yaml.set("protection.explosions", true);
        yaml.set("protection.max-more-user-signs", 4);
        yaml.set("protection.extension-placement-order", List.of("NORTH", "SOUTH", "EAST", "WEST"));
        yaml.set("protection.lockable-materials", List.of("BARREL", "CHEST", "HOPPER"));
        return new SignLockConfig(yaml);
    }

    private InventoryMoveItemEvent mockInventoryMoveEvent(InventoryHolder sourceHolder, InventoryHolder destinationHolder) {
        Inventory source = Mockito.mock(Inventory.class);
        when(source.getHolder()).thenReturn(sourceHolder);
        Inventory destination = Mockito.mock(Inventory.class);
        when(destination.getHolder()).thenReturn(destinationHolder);
        InventoryMoveItemEvent event = Mockito.mock(InventoryMoveItemEvent.class);
        when(event.getSource()).thenReturn(source);
        when(event.getDestination()).thenReturn(destination);
        when(event.getItem()).thenReturn(new ItemStack(Material.STONE));
        return event;
    }

    private InventoryHolder mockDoubleChestHolder(Block locationBlock) {
        org.bukkit.block.DoubleChest holder = Mockito.mock(org.bukkit.block.DoubleChest.class);
        when(holder.getLocation()).thenReturn(locationBlock.getLocation());
        return holder;
    }

    private Block protectedBarrel(int x, int y, int z, String owner) {
        Block barrel = world.getBlockAt(x, y, z);
        barrel.setType(Material.BARREL);
        managedWallSign(barrel, BlockFace.NORTH, "[private]", owner, "", "");
        return barrel;
    }

    private Block lockedHopper(int x, int y, int z, String owner) {
        Block hopper = world.getBlockAt(x, y, z);
        hopper.setType(Material.HOPPER);
        managedWallSign(hopper, BlockFace.NORTH, "[private]", owner, "", "");
        return hopper;
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

    private Sign managedWallSign(Block target, BlockFace signFace, String... lines) {
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
