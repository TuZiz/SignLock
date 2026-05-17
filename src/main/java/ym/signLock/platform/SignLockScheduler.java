package ym.signLock.platform;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Consumer;

public interface SignLockScheduler {

    void runNextTick(Runnable task);

    void runAtPlayer(Player player, Runnable task);

    void runAtBlock(Block block, Runnable task);

    static SignLockScheduler create(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        if (ReflectiveFoliaScheduler.isAvailable(plugin)) {
            return new ReflectiveFoliaScheduler(plugin);
        }
        return new BukkitSignLockScheduler(plugin);
    }

    static SignLockScheduler immediate() {
        return new SignLockScheduler() {
            @Override
            public void runNextTick(Runnable task) {
                task.run();
            }

            @Override
            public void runAtPlayer(Player player, Runnable task) {
                task.run();
            }

            @Override
            public void runAtBlock(Block block, Runnable task) {
                task.run();
            }
        };
    }
}

final class BukkitSignLockScheduler implements SignLockScheduler {

    private final JavaPlugin plugin;

    BukkitSignLockScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runNextTick(Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    @Override
    public void runAtPlayer(Player player, Runnable task) {
        runNextTick(task);
    }

    @Override
    public void runAtBlock(Block block, Runnable task) {
        runNextTick(task);
    }
}

final class ReflectiveFoliaScheduler implements SignLockScheduler {

    private final JavaPlugin plugin;

    ReflectiveFoliaScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    static boolean isAvailable(JavaPlugin plugin) {
        try {
            plugin.getServer().getClass().getMethod("getGlobalRegionScheduler");
            plugin.getServer().getClass().getMethod("getRegionScheduler");
            return true;
        } catch (NoSuchMethodException exception) {
            return false;
        }
    }

    @Override
    public void runNextTick(Runnable task) {
        try {
            Object scheduler = plugin.getServer().getClass().getMethod("getGlobalRegionScheduler").invoke(plugin.getServer());
            Method run = scheduler.getClass().getMethod("run", org.bukkit.plugin.Plugin.class, Consumer.class);
            run.invoke(scheduler, plugin, (Consumer<Object>) ignored -> task.run());
        } catch (ReflectiveOperationException exception) {
            throw schedulerException("global region", exception);
        }
    }

    @Override
    public void runAtPlayer(Player player, Runnable task) {
        if (player == null) {
            runNextTick(task);
            return;
        }

        try {
            Object scheduler = player.getClass().getMethod("getScheduler").invoke(player);
            Method run = scheduler.getClass().getMethod("run", org.bukkit.plugin.Plugin.class, Consumer.class, Runnable.class);
            run.invoke(scheduler, plugin, (Consumer<Object>) ignored -> task.run(), (Runnable) () -> {
            });
        } catch (ReflectiveOperationException exception) {
            throw schedulerException("player", exception);
        }
    }

    @Override
    public void runAtBlock(Block block, Runnable task) {
        if (block == null) {
            runNextTick(task);
            return;
        }

        try {
            Object scheduler = plugin.getServer().getClass().getMethod("getRegionScheduler").invoke(plugin.getServer());
            Method run = scheduler.getClass().getMethod("run", org.bukkit.plugin.Plugin.class, Location.class, Consumer.class);
            run.invoke(scheduler, plugin, block.getLocation(), (Consumer<Object>) ignored -> task.run());
        } catch (ReflectiveOperationException exception) {
            throw schedulerException("region", exception);
        }
    }

    private IllegalStateException schedulerException(String schedulerType, ReflectiveOperationException exception) {
        Throwable cause = exception instanceof InvocationTargetException invocation && invocation.getCause() != null
                ? invocation.getCause()
                : exception;
        return new IllegalStateException("Failed to schedule SignLock task on Folia " + schedulerType + " scheduler", cause);
    }
}
