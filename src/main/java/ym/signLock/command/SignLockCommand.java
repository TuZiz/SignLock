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
import ym.signLock.service.LockService.AddPlayerResult;
import ym.signLock.service.LockService.LockDetails;
import ym.signLock.service.LockService.LockInfo;
import ym.signLock.service.LockService.RemovePlayerResult;

import java.util.List;

public final class SignLockCommand implements CommandExecutor, TabCompleter {

    private final SignLock plugin;

    public SignLockCommand(SignLock plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            return handleAdd(sender, args[1].trim());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            return handleRemove(sender, args[1].trim());
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

    private boolean handleAdd(CommandSender sender, String targetPlayer) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getSignLockConfig().addInvalidSignMessage());
            return true;
        }
        if (!sender.hasPermission("signlock.add")) {
            sender.sendMessage(plugin.getSignLockConfig().noPermissionMessage());
            return true;
        }
        if (targetPlayer.isEmpty()) {
            sender.sendMessage(plugin.getSignLockConfig().addPlayerNotFoundMessage());
            return true;
        }

        targetPlayer = normalizeTargetPlayer(player, targetPlayer);

        TargetedLock targetedLock = getTargetedLock(player);
        if (targetedLock == null) {
            sender.sendMessage(plugin.getSignLockConfig().addInvalidSignMessage());
            return true;
        }
        if (!plugin.getLockService().canManage(targetedLock.lock(), player)) {
            sender.sendMessage(plugin.getSignLockConfig().addOnlyOwnerMessage());
            return true;
        }

        AddPlayerResult result = plugin.getLockService().addPlayerToLock(targetedLock.sign(), targetedLock.lock(), targetPlayer);
        if (result == AddPlayerResult.ALREADY_AUTHORIZED) {
            sender.sendMessage(plugin.getSignLockConfig().addAlreadyAuthorizedMessage(targetPlayer));
            return true;
        }
        if (result == AddPlayerResult.NO_SPACE) {
            sender.sendMessage(plugin.getSignLockConfig().addListFullMessage());
            return true;
        }
        if (result == AddPlayerResult.ADDED_WITH_EXTENSION) {
            sender.sendMessage(plugin.getSignLockConfig().extensionCreatedMessage());
        }

        plugin.getPlayerIdentityService().save();
        sender.sendMessage(plugin.getSignLockConfig().addSuccessMessage(targetPlayer));
        return true;
    }

    private boolean handleRemove(CommandSender sender, String targetPlayer) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getSignLockConfig().addInvalidSignMessage());
            return true;
        }
        if (!sender.hasPermission("signlock.add")) {
            sender.sendMessage(plugin.getSignLockConfig().noPermissionMessage());
            return true;
        }
        if (targetPlayer.isEmpty()) {
            sender.sendMessage(plugin.getSignLockConfig().addPlayerNotFoundMessage());
            return true;
        }

        targetPlayer = normalizeTargetPlayer(player, targetPlayer);

        TargetedLock targetedLock = getTargetedLock(player);
        if (targetedLock == null) {
            sender.sendMessage(plugin.getSignLockConfig().addInvalidSignMessage());
            return true;
        }
        if (!plugin.getLockService().canManage(targetedLock.lock(), player)) {
            sender.sendMessage(plugin.getSignLockConfig().addOnlyOwnerMessage());
            return true;
        }

        RemovePlayerResult result = plugin.getLockService().removePlayerFromLock(targetedLock.sign(), targetedLock.lock(), targetPlayer);
        if (result == RemovePlayerResult.OWNER_DENIED) {
            sender.sendMessage(plugin.getSignLockConfig().removeOwnerDeniedMessage());
            return true;
        }
        if (result == RemovePlayerResult.NOT_FOUND) {
            sender.sendMessage(plugin.getSignLockConfig().removeNotFoundMessage(targetPlayer));
            return true;
        }

        sender.sendMessage(plugin.getSignLockConfig().removeSuccessMessage(targetPlayer));
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

        sender.sendMessage(plugin.getSignLockConfig().infoHeaderMessage());
        sender.sendMessage(plugin.getSignLockConfig().infoOwnerMessage(details.owner()));
        if (details.allowedPlayers().isEmpty()) {
            sender.sendMessage(plugin.getSignLockConfig().infoNoPlayersMessage());
        } else {
            sender.sendMessage(plugin.getSignLockConfig().infoPlayersMessage(String.join(", ", details.allowedPlayers())));
        }
        sender.sendMessage(plugin.getSignLockConfig().infoExtensionsMessage(details.extensionCount()));
        return true;
    }

    private String normalizeTargetPlayer(Player actor, String targetPlayer) {
        plugin.getPlayerIdentityService().remember(actor);
        String normalizedTarget = plugin.getPlayerIdentityService().resolveStoredName(targetPlayer);
        plugin.getPlayerIdentityService().save();
        return normalizedTarget == null || normalizedTarget.isBlank() ? targetPlayer : normalizedTarget;
    }

    private TargetedLock getTargetedLock(Player player) {
        Block targetBlock = player.getTargetBlockExact(6);
        if (!(targetBlock != null && targetBlock.getState() instanceof Sign sign)) {
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
