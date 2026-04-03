package ym.signLock.gui;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import ym.signLock.config.SignLockConfig;
import ym.signLock.service.LockService;

import java.util.Locale;

public record LockSummaryTarget(String blockType, String worldName, int x, int y, int z, LockService.LockTargetKind kind) {

    public static LockSummaryTarget from(Block target) {
        LockService.LockTargetKind resolvedKind = LockService.LockTargetKind.GENERIC;
        BlockData data = target.getBlockData();
        if (data instanceof Chest chest) {
            resolvedKind = chest.getType() == Chest.Type.SINGLE
                    ? LockService.LockTargetKind.SINGLE_CHEST
                    : LockService.LockTargetKind.DOUBLE_CHEST;
        }
        return new LockSummaryTarget(
                target.getType().name(),
                target.getWorld().getName(),
                target.getX(),
                target.getY(),
                target.getZ(),
                resolvedKind
        );
    }

    public static LockSummaryTarget from(LockService.LockTargetDetails target) {
        return new LockSummaryTarget(
                target.blockType(),
                target.worldName(),
                target.x(),
                target.y(),
                target.z(),
                target.kind()
        );
    }

    public String summaryLabel(SignLockConfig config) {
        return switch (kind) {
            case SINGLE_CHEST -> config.summarySingleChestTargetLabel(worldName, x, y, z);
            case DOUBLE_CHEST -> config.summaryDoubleChestTargetLabel(worldName, x, y, z);
            case GENERIC -> config.summaryGenericTargetLabel(humanizeBlockType(), worldName, x, y, z);
        };
    }

    private String humanizeBlockType() {
        return blockType.toLowerCase(Locale.ROOT).replace('_', ' ');
    }
}
