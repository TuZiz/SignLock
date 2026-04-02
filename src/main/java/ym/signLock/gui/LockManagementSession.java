package ym.signLock.gui;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import ym.signLock.service.LockService;

import java.util.UUID;

public record LockManagementSession(UUID worldId, int x, int y, int z, Location signLocation) {

    public LockManagementSession {
        signLocation = signLocation == null ? null : signLocation.clone();
    }

    public static LockManagementSession from(LockService.LockInfo lock) {
        Block target = lock.targetBlock();
        return new LockManagementSession(
                target.getWorld().getUID(),
                target.getX(),
                target.getY(),
                target.getZ(),
                lock.signBlock().getLocation()
        );
    }

    @Override
    public Location signLocation() {
        return signLocation == null ? null : signLocation.clone();
    }

    public Block resolveSignBlock() {
        if (signLocation == null) {
            return null;
        }
        World world = signLocation.getWorld();
        return world == null ? null : signLocation.getBlock();
    }
}
