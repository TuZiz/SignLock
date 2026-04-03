package ym.signLock.gui;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import ym.signLock.config.SignLockConfig;
import ym.signLock.service.LockBatchAuthorizationService;
import ym.signLock.service.LockBatchTargetParser;
import ym.signLock.service.LockPlayerNameNormalizer;
import ym.signLock.service.LockService;

public final class LockManagementGuiActionService {

    private final LockService lockService;
    private final LockBatchTargetParser batchTargetParser;
    private final LockBatchAuthorizationService batchAuthorizationService;
    private final LockPlayerNameNormalizer playerNameNormalizer;
    private final LockManagementGuiService guiService;
    private final LockManagementPendingInputStore pendingInputStore;
    private SignLockConfig config;

    public LockManagementGuiActionService(
            LockService lockService,
            LockBatchTargetParser batchTargetParser,
            LockBatchAuthorizationService batchAuthorizationService,
            LockPlayerNameNormalizer playerNameNormalizer,
            LockManagementGuiService guiService,
            LockManagementPendingInputStore pendingInputStore,
            SignLockConfig config
    ) {
        this.lockService = lockService;
        this.batchTargetParser = batchTargetParser;
        this.batchAuthorizationService = batchAuthorizationService;
        this.playerNameNormalizer = playerNameNormalizer;
        this.guiService = guiService;
        this.pendingInputStore = pendingInputStore;
        this.config = config;
    }

    public void setConfig(SignLockConfig config) {
        this.config = config;
    }

    public void handleClick(Player player, LockManagementGuiHolder holder, int rawSlot) {
        if (rawSlot == LockManagementGui.REFRESH_SLOT) {
            guiService.openFor(player, holder.session());
            return;
        }

        if (rawSlot == LockManagementGui.CLOSE_SLOT) {
            player.closeInventory();
            return;
        }

        if (holder.readOnly()) {
            player.sendMessage(config.guiReadOnlyMessage());
            return;
        }

        if (rawSlot == LockManagementGui.ADD_SLOT) {
            pendingInputStore.beginAdd(player.getUniqueId(), holder.session());
            player.closeInventory();
            player.sendMessage(config.guiAddPromptMessage());
            return;
        }

        if (rawSlot == LockManagementGui.REMOVE_SELECTED_SLOT) {
            removeSelectedPlayers(player, holder);
            return;
        }

        String playerName = holder.removablePlayerAt(rawSlot);
        if (playerName != null) {
            holder.toggleSelectedPlayer(rawSlot);
            guiService.openFor(player, holder);
        }
    }

    public void handleChatInput(Player player, String rawInput) {
        LockManagementPendingInputStore.PendingInput pending = pendingInputStore.consume(player.getUniqueId());
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

        var targets = batchTargetParser.parse(message);
        if (targets.isEmpty()) {
            player.sendMessage(config.addPlayerNotFoundMessage());
            guiService.openFor(player, pending.session());
            return;
        }

        LockBatchAuthorizationService.BatchAddSummary summary =
                batchAuthorizationService.addPlayers(player, resolvedLock.sign(), resolvedLock.lock(), targets);
        if (!summary.permitted()) {
            player.sendMessage(config.addOnlyOwnerMessage());
            return;
        }

        player.sendMessage(config.batchAddSummaryMessage(
                summary.addedPlayers(),
                summary.alreadyAuthorizedPlayers(),
                summary.noSpacePlayers()
        ));
        guiService.openFor(player, pending.session());
    }

    private void removeSelectedPlayers(Player player, LockManagementGuiHolder holder) {
        if (!holder.hasSelection()) {
            player.sendMessage(config.guiRemoveSelectionEmptyMessage());
            guiService.openFor(player, holder);
            return;
        }

        ResolvedLock resolvedLock = resolve(holder.session());
        if (resolvedLock == null || !lockService.canManage(resolvedLock.lock(), player)) {
            player.sendMessage(config.addInvalidSignMessage());
            return;
        }

        LockBatchAuthorizationService.BatchRemoveSummary summary =
                batchAuthorizationService.removePlayers(player, resolvedLock.sign(), resolvedLock.lock(), holder.selectedPlayers());
        if (!summary.permitted()) {
            player.sendMessage(config.addOnlyOwnerMessage());
            return;
        }

        player.sendMessage(config.batchRemoveSummaryMessage(
                summary.removedPlayers(),
                summary.notFoundPlayers(),
                summary.ownerDeniedPlayers()
        ));
        guiService.openFor(player, holder.session());
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
