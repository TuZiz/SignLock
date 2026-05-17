package ym.signLock.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

public final class PaperAsyncChatBridgeRegistrar {

    private static final String PAPER_ASYNC_CHAT_EVENT = "io.papermc.paper.event.player.AsyncChatEvent";
    private static final String PLAIN_TEXT_SERIALIZER = "net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer";

    private PaperAsyncChatBridgeRegistrar() {
    }

    @SuppressWarnings("unchecked")
    public static void register(JavaPlugin plugin, LockGuiChatInputListener chatInputListener) {
        try {
            Class<?> eventClass = Class.forName(PAPER_ASYNC_CHAT_EVENT);
            if (!Event.class.isAssignableFrom(eventClass)) {
                return;
            }

            PluginManager pluginManager = plugin.getServer().getPluginManager();
            Listener bridgeListener = new Listener() {
            };
            EventExecutor executor = (ignored, event) -> {
                try {
                    handleEvent(eventClass, event, chatInputListener);
                } catch (ReflectiveOperationException exception) {
                    plugin.getLogger().warning("Failed to handle Paper AsyncChatEvent bridge: " + exception.getMessage());
                }
            };

            pluginManager.registerEvent(
                    (Class<? extends Event>) eventClass,
                    bridgeListener,
                    EventPriority.LOWEST,
                    executor,
                    plugin,
                    false
            );
        } catch (ClassNotFoundException ignored) {
            // Running on Spigot or an older API where AsyncPlayerChatEvent is still the active path.
        } catch (LinkageError exception) {
            plugin.getLogger().warning("Failed to register Paper AsyncChatEvent bridge: " + exception.getMessage());
        }
    }

    static void handleEvent(Class<?> eventClass, Object rawEvent, LockGuiChatInputListener chatInputListener) throws ReflectiveOperationException {
        if (!eventClass.isInstance(rawEvent)) {
            return;
        }

        Player player = (Player) eventClass.getMethod("getPlayer").invoke(rawEvent);
        if (!chatInputListener.hasPendingAdd(player)) {
            return;
        }

        String message = resolveMessage(rawEvent, eventClass);
        suppressPaperChat(rawEvent, eventClass);
        chatInputListener.capturePendingInput(player, message);
    }

    private static String resolveMessage(Object rawEvent, Class<?> eventClass) throws ReflectiveOperationException {
        Object message = invokeOptional(eventClass, rawEvent, "message");
        if (message == null) {
            message = invokeOptional(eventClass, rawEvent, "originalMessage");
        }
        if (message == null) {
            Object legacy = invokeOptional(eventClass, rawEvent, "getMessage");
            return legacy == null ? "" : legacy.toString();
        }
        return toPlainText(message);
    }

    private static Object invokeOptional(Class<?> eventClass, Object rawEvent, String methodName) throws ReflectiveOperationException {
        try {
            Method method = eventClass.getMethod(methodName);
            return method.invoke(rawEvent);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static String toPlainText(Object component) {
        try {
            Class<?> serializerClass = Class.forName(PLAIN_TEXT_SERIALIZER);
            Object serializer = serializerClass.getMethod("plainText").invoke(null);
            Object result = serializerClass.getMethod("serialize", Class.forName("net.kyori.adventure.text.Component"))
                    .invoke(serializer, component);
            return result == null ? "" : result.toString();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return component == null ? "" : component.toString();
        }
    }

    private static void suppressPaperChat(Object rawEvent, Class<?> eventClass) throws ReflectiveOperationException {
        eventClass.getMethod("setCancelled", boolean.class).invoke(rawEvent, true);

        Object viewers = invokeOptional(eventClass, rawEvent, "viewers");
        if (viewers instanceof Iterable<?> iterable) {
            if (viewers instanceof java.util.Collection<?> collection) {
                collection.clear();
            } else {
                for (Object ignored : iterable) {
                    break;
                }
            }
        }

        try {
            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            Object emptyComponent = componentClass.getMethod("text", String.class).invoke(null, "");
            eventClass.getMethod("message", componentClass).invoke(rawEvent, emptyComponent);
        } catch (NoSuchMethodException | ClassNotFoundException | LinkageError ignored) {
            // Some implementations may not expose a mutable message setter.
        }
    }
}
