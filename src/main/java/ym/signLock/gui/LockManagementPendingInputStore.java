package ym.signLock.gui;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LockManagementPendingInputStore {

    private final Map<UUID, PendingInput> pendingInputs = new ConcurrentHashMap<>();

    public void beginAdd(UUID playerId, LockManagementSession session) {
        pendingInputs.put(playerId, new PendingInput(InputMode.BATCH_ADD, session));
    }

    public boolean hasPendingAdd(UUID playerId) {
        PendingInput pendingInput = pendingInputs.get(playerId);
        return pendingInput != null && pendingInput.mode() == InputMode.BATCH_ADD;
    }

    public PendingInput consume(UUID playerId) {
        return pendingInputs.remove(playerId);
    }

    public enum InputMode {
        BATCH_ADD
    }

    public record PendingInput(InputMode mode, LockManagementSession session) {
    }
}
