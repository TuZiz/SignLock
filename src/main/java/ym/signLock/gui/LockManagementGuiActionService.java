package ym.signLock.gui;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import ym.signLock.config.SignLockConfig;
import ym.signLock.service.LockPlayerNameNormalizer;
import ym.signLock.service.LockService;

public final class LockManagementGuiActionService {

    private final LockService lockService;
    private final LockPlayerNameNormalizer playerNameNormalizer;
    private final LockManagementGuiService guiService;
    private final LockManagementPendingInputStore pendingInputStore;
    private SignLockConfig config;

    public LockManagementGuiActionService(
            LockService lockService,
            LockPlayerNameNormalizer playerNameNormalizer,
            LockManagementGuiService guiService,
            LockManagementPendingInputStore pendingInputStore,
            SignLockConfig config
    ) {
        this.lockService = lockService;
        this.playerNameNormalizer = playerNameNormalizer;
        this.guiService = guiService;
        this.pendingInputStore = pendingInputStore;
        this.config = config;
    }

    public void setConfig(SignLockConfig config) {
        this.config = config;
    }

    public void handleClick(Player player, LockManagementGuiHolder holder, int rawSlot) {
        if (rawSlot == LockManagementGui.ADD_SLOT) {
            pendingInputStore.beginAdd(player.getUniqueId(), holder.session());
            player.closeInventory();
            player.sendMessage(config.guiAddPromptMessage());
            return;
        }

        if (rawSlot == LockManagementGui.REFRESH_SLOT) {
            guiService.openFor(player, holder.session());
            return;
        }

        if (rawSlot == LockManagementGui.CLOSE_SLOT) {
            player.closeInventory();
            return;
        }

        String playerName = holder.removablePlayerAt(rawSlot);
        if (playerName != null) {
            removePlayer(player, holder.session(), playerName);
        }
    }

    public void handleChatInput(Player player, String rawInput) {
        LockManagementPendingInputStore.PendingAddInput pending = pendingInputStore.consume(player.getUniqueId());
        if (pending == null) {
            return;
        }

        String message = rawInput == null ? "" : rawInput.trim();
        if (message.isEmpty()) {
            player.sendMessage(config.addPlayerNotFoundMessage());
            guiService.openFor(player, pending.session());
            return;
        }

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(config.guiAddCancelledMessage());
            guiService.openFor(player, pending.session());
            return;
        }

        ResolvedLock resolvedLock = resolve(pending.session());
        if (resolvedLock == null || !lockService.canManage(resolvedLock.lock(), player)) {
            player.sendMessage(config.addInvalidSignMessage());
            return;
        }

        String normalizedTarget = playerNameNormalizer.normalize(player, message);
        LockService.AddPlayerResult result = lockService.addPlayerToLock(resolvedLock.sign(), resolvedLock.lock(), normalizedTarget);
        if (result == LockService.AddPlayerResult.ALREADY_AUTHORIZED) {
            player.sendMessage(config.addAlreadyAuthorizedMessage(normalizedTarget));
        } else if (result == LockService.AddPlayerResult.NO_SPACE) {
            player.sendMessage(config.addListFullMessage());
        } else {
            if (result == LockService.AddPlayerResult.ADDED_WITH_EXTENSION) {
                player.sendMessage(config.extensionCreatedMessage());
            }
            player.sendMessage(config.addSuccessMessage(normalizedTarget));
        }
        guiService.openFor(player, pending.session());
    }

    private void removePlayer(Player player, LockManagementSession session, String targetPlayer) {
        ResolvedLock resolvedLock = resolve(session);
        if (resolvedLock == null || !lockService.canManage(resolvedLock.lock(), player)) {
            player.sendMessage(config.addInvalidSignMessage());
            return;
        }

        LockService.RemovePlayerResult result = lockService.removePlayerFromLock(resolvedLock.sign(), resolvedLock.lock(), targetPlayer);
        if (result == LockService.RemovePlayerResult.OWNER_DENIED) {
            player.sendMessage(config.removeOwnerDeniedMessage());
        } else if (result == LockService.RemovePlayerResult.NOT_FOUND) {
            player.sendMessage(config.removeNotFoundMessage(targetPlayer));
        } else {
            player.sendMessage(config.removeSuccessMessage(targetPlayer));
        }
        guiService.openFor(player, session);
    }

    private ResolvedLock resolve(LockManagementSession session) {
        Block signBlock = session.resolveSignBlock();
        if (!(signBlock != null && signBlock.getState() instanceof Sign sign)) {
            return null;
        }

        LockService.LockInfo lock = lockService.findManagedSignLock(signBlock);
        return lock == null ? null : new ResolvedLock(sign, lock);
    }

    private record ResolvedLock(Sign sign, LockService.LockInfo lock) {
    }
}
