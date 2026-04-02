package ym.signLock.gui;

import org.bukkit.block.Block;

public record LockSummaryTarget(String blockType, String worldName, int x, int y, int z) {

    public static LockSummaryTarget from(Block target) {
        return new LockSummaryTarget(
                target.getType().name(),
                target.getWorld().getName(),
                target.getX(),
                target.getY(),
                target.getZ()
        );
    }
}
