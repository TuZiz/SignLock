package ym.signLock.command;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ym.signLock.SignLock;
import ym.signLock.gui.LockSummaryView;
import ym.signLock.service.LockBatchAuthorizationService;
import ym.signLock.service.LockBatchTargetParser;
import ym.signLock.service.LockPlayerNameNormalizer;
import ym.signLock.service.LockService;
import ym.signLock.service.LockService.AddPlayerResult;
import ym.signLock.service.LockService.LockDetails;
import ym.signLock.service.LockService.LockInfo;
import ym.signLock.service.LockService.RemovePlayerResult;

import java.util.Arrays;
import java.util.List;

public final class SignLockCommand implements CommandExecutor, TabCompleter {

    private final SignLock plugin;

    public SignLockCommand(SignLock plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length >= 2 && args[0].equalsIgnoreCase("add")) {
            return handleAdd(sender, joinTargets(args));
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("remove")) {
            return handleRemove(sender, joinTargets(args));
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
            return handleInfo(sender);
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("signlock.reload")) {
                sender.sendMessage(plugin.getSignLockConfig().noPermissionMessage());
                return true;
            }

            plugin.reloadPluginConfig();
            sender.sendMessage(plugin.getSignLockConfig().reloadSuccessMessage());
            return true;
        }

        sender.sendMessage(plugin.getSignLockConfig().addUsageMessage());
        sender.sendMessage(plugin.getSignLockConfig().removeUsageMessage());
        sender.sendMessage(plugin.getSignLockConfig().infoUsageMessage());
        sender.sendMessage(plugin.getSignLockConfig().reloadUsageMessage());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("reload", "add", "remove", "info");
        }
        return List.of();
    }

    private boolean handleAdd(CommandSender sender, String rawTargets) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getSignLockConfig().addInvalidSignMessage());
            return true;
        }
        if (!sender.hasPermission("signlock.add")) {
            sender.sendMessage(plugin.getSignLockConfig().noPermissionMessage());
            return true;
        }
        List<String> targets = batchTargetParser().parse(rawTargets);
        if (targets.isEmpty()) {
            sender.sendMessage(plugin.getSignLockConfig().addPlayerNotFoundMessage());
            return true;
        }

        TargetedLock targetedLock = getTargetedLock(player);
        if (targetedLock == null) {
            sender.sendMessage(plugin.getSignLockConfig().addInvalidSignMessage());
            return true;
        }
        if (!plugin.getLockService().canManage(targetedLock.lock(), player)) {
            sender.sendMessage(plugin.getSignLockConfig().addOnlyOwnerMessage());
            return true;
        }

        LockBatchAuthorizationService.BatchAddSummary summary = batchAuthorizationService()
                .addPlayers(player, targetedLock.sign(), targetedLock.lock(), targets);
        if (!summary.permitted()) {
            sender.sendMessage(plugin.getSignLockConfig().addOnlyOwnerMessage());
            return true;
        }

        if (targets.size() == 1) {
            return sendSingleAddResult(sender, summary);
        }

        sender.sendMessage(plugin.getSignLockConfig().batchAddSummaryMessage(
                summary.addedPlayers(),
                summary.alreadyAuthorizedPlayers(),
                summary.noSpacePlayers()
        ));
        return true;
    }

    private boolean handleRemove(CommandSender sender, String rawTargets) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getSignLockConfig().addInvalidSignMessage());
            return true;
        }
        if (!sender.hasPermission("signlock.add")) {
            sender.sendMessage(plugin.getSignLockConfig().noPermissionMessage());
            return true;
        }
        List<String> targets = batchTargetParser().parse(rawTargets);
        if (targets.isEmpty()) {
            sender.sendMessage(plugin.getSignLockConfig().addPlayerNotFoundMessage());
            return true;
        }

        TargetedLock targetedLock = getTargetedLock(player);
        if (targetedLock == null) {
            sender.sendMessage(plugin.getSignLockConfig().addInvalidSignMessage());
            return true;
        }
        if (!plugin.getLockService().canManage(targetedLock.lock(), player)) {
            sender.sendMessage(plugin.getSignLockConfig().addOnlyOwnerMessage());
            return true;
        }

        LockBatchAuthorizationService.BatchRemoveSummary summary = batchAuthorizationService()
                .removePlayers(player, targetedLock.sign(), targetedLock.lock(), targets);
        if (!summary.permitted()) {
            sender.sendMessage(plugin.getSignLockConfig().addOnlyOwnerMessage());
            return true;
        }

        if (targets.size() == 1) {
            return sendSingleRemoveResult(sender, summary);
        }

        sender.sendMessage(plugin.getSignLockConfig().batchRemoveSummaryMessage(
                summary.removedPlayers(),
                summary.notFoundPlayers(),
                summary.ownerDeniedPlayers()
        ));
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getSignLockConfig().addInvalidSignMessage());
            return true;
        }

        TargetedLock targetedLock = getTargetedLock(player);
        if (targetedLock == null) {
            sender.sendMessage(plugin.getSignLockConfig().addInvalidSignMessage());
            return true;
        }

        LockDetails details = plugin.getLockService().describeLock(targetedLock.sign().getBlock());
        if (details == null) {
            sender.sendMessage(plugin.getSignLockConfig().addInvalidSignMessage());
            return true;
        }

        LockSummaryView view = LockSummaryView.from(
                targetedLock.lock(),
                details,
                plugin.getLockService().viewerScope(targetedLock.lock(), player)
        );

        sender.sendMessage(plugin.getSignLockConfig().infoHeaderMessage());
        sender.sendMessage(plugin.getSignLockConfig().infoOwnerMessage(view.owner()));
        sender.sendMessage(plugin.getSignLockConfig().infoScopeMessage(view.scopeLabel(plugin.getSignLockConfig())));
        sender.sendMessage(plugin.getSignLockConfig().infoTargetMessage(view.target().summaryLabel(plugin.getSignLockConfig())));
        if (!view.canViewAuthorizedPlayers()) {
            sender.sendMessage(plugin.getSignLockConfig().infoPlayersHiddenMessage());
        } else if (view.allowedPlayers().isEmpty()) {
            sender.sendMessage(plugin.getSignLockConfig().infoNoPlayersMessage());
        } else {
            sender.sendMessage(plugin.getSignLockConfig().infoPlayersMessage(String.join(", ", view.allowedPlayers())));
        }
        sender.sendMessage(plugin.getSignLockConfig().infoExtensionsMessage(view.extensionCount()));
        return true;
    }

    private String normalizeTargetPlayer(Player actor, String targetPlayer) {
        return new LockPlayerNameNormalizer(plugin.getPlayerIdentityService()).normalize(actor, targetPlayer);
    }

    private LockBatchTargetParser batchTargetParser() {
        return new LockBatchTargetParser();
    }

    private LockBatchAuthorizationService batchAuthorizationService() {
        return new LockBatchAuthorizationService(plugin.getLockService(), new LockPlayerNameNormalizer(plugin.getPlayerIdentityService()));
    }

    private boolean sendSingleAddResult(CommandSender sender, LockBatchAuthorizationService.BatchAddSummary summary) {
        if (!summary.addedPlayers().isEmpty()) {
            String playerName = summary.addedPlayers().get(0);
            if (summary.addedWithExtensionPlayers().contains(playerName)) {
                sender.sendMessage(plugin.getSignLockConfig().extensionCreatedMessage());
            }
            sender.sendMessage(plugin.getSignLockConfig().addSuccessMessage(playerName));
            return true;
        }
        if (!summary.alreadyAuthorizedPlayers().isEmpty()) {
            sender.sendMessage(plugin.getSignLockConfig().addAlreadyAuthorizedMessage(summary.alreadyAuthorizedPlayers().get(0)));
            return true;
        }
        sender.sendMessage(plugin.getSignLockConfig().addListFullMessage());
        return true;
    }

    private boolean sendSingleRemoveResult(CommandSender sender, LockBatchAuthorizationService.BatchRemoveSummary summary) {
        if (!summary.removedPlayers().isEmpty()) {
            sender.sendMessage(plugin.getSignLockConfig().removeSuccessMessage(summary.removedPlayers().get(0)));
            return true;
        }
        if (!summary.ownerDeniedPlayers().isEmpty()) {
            sender.sendMessage(plugin.getSignLockConfig().removeOwnerDeniedMessage());
            return true;
        }
        sender.sendMessage(plugin.getSignLockConfig().removeNotFoundMessage(summary.notFoundPlayers().get(0)));
        return true;
    }

    private String joinTargets(String[] args) {
        return String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
    }

    private TargetedLock getTargetedLock(Player player) {
        Block targetBlock = player.getTargetBlockExact(6);
        Sign sign = LockService.getSignStateIfSign(targetBlock);
        if (sign == null) {
            return null;
        }

        LockInfo lock = plugin.getLockService().findManagedSignLock(targetBlock);
        if (lock == null) {
            return null;
        }

        return new TargetedLock(sign, lock);
    }

    private record TargetedLock(Sign sign, LockInfo lock) {
    }
}
