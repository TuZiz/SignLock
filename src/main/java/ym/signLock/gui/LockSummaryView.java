package ym.signLock.gui;

import ym.signLock.config.SignLockConfig;
import ym.signLock.service.LockService;

import java.util.List;

public record LockSummaryView(
        String owner,
        List<String> allowedPlayers,
        int extensionCount,
        LockSummaryTarget target,
        LockService.LockViewerScope viewerScope
) {

    public LockSummaryView {
        allowedPlayers = List.copyOf(allowedPlayers);
    }

    public static LockSummaryView from(LockService.LockInfo lock, LockService.LockDetails details) {
        return from(lock, details, LockService.LockViewerScope.MANAGE);
    }

    public static LockSummaryView from(
            LockService.LockInfo lock,
            LockService.LockDetails details,
            LockService.LockViewerScope viewerScope
    ) {
        return new LockSummaryView(
                details.owner(),
                details.allowedPlayers(),
                details.extensionCount(),
                details.target() == null ? LockSummaryTarget.from(lock.targetBlock()) : LockSummaryTarget.from(details.target()),
                viewerScope
        );
    }

    public boolean readOnly() {
        return viewerScope.readOnly();
    }

    public boolean canManage() {
        return viewerScope == LockService.LockViewerScope.MANAGE;
    }

    public boolean canViewAuthorizedPlayers() {
        return viewerScope.canViewAuthorizedPlayers();
    }

    public String scopeLabel(SignLockConfig config) {
        return switch (viewerScope) {
            case MANAGE -> config.scopeManageLabel();
            case ACCESS -> config.scopeAccessLabel();
            case DENIED -> config.scopeDeniedLabel();
        };
    }
}
