package ym.signLock.gui;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LockManagementPendingInputStore {

    private final Map<UUID, PendingAddInput> pendingAdds = new ConcurrentHashMap<>();

    public void beginAdd(UUID playerId, LockManagementSession session) {
        pendingAdds.put(playerId, new PendingAddInput(session));
    }

    public boolean hasPendingAdd(UUID playerId) {
        return pendingAdds.containsKey(playerId);
    }

    public PendingAddInput consume(UUID playerId) {
        return pendingAdds.remove(playerId);
    }

    public record PendingAddInput(LockManagementSession session) {
    }
}
