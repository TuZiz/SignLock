package ym.signLock.gui;

import ym.signLock.service.LockService;

import java.util.List;

public record LockSummaryView(String owner, List<String> allowedPlayers, int extensionCount, LockSummaryTarget target) {

    public LockSummaryView {
        allowedPlayers = List.copyOf(allowedPlayers);
    }

    public static LockSummaryView from(LockService.LockInfo lock, LockService.LockDetails details) {
        return new LockSummaryView(
                details.owner(),
                details.allowedPlayers(),
                details.extensionCount(),
                LockSummaryTarget.from(lock.targetBlock())
        );
    }
}
