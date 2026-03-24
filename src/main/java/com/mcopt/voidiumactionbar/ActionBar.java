package com.mcopt.voidiumactionbar;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.bukkit.entity.Player;

final class ActionBar {
  private ActionBar() {}

  static void send(Player player, String message) {
    if (message == null) {
      return;
    }

    // 1) Some versions expose Player#sendActionBar(String)
    if (tryPlayerSendActionBarString(player, message)) {
      return;
    }

    // 2) Paper/Adventure: Player#sendActionBar(Component)
    if (tryAdventure(player, message)) {
      return;
    }

    // 3) Spigot/Bungee: player.spigot().sendMessage(ChatMessageType.ACTION_BAR, BaseComponent...)
    if (trySpigot(player, message)) {
      return;
    }

    // Fallback
    player.sendMessage(message);
  }

  private static boolean tryPlayerSendActionBarString(Player player, String message) {
    try {
      Method m = player.getClass().getMethod("sendActionBar", String.class);
      m.invoke(player, message);
      return true;
    } catch (ReflectiveOperationException ignored) {
      return false;
    }
  }

  private static boolean tryAdventure(Player player, String message) {
    try {
      ClassLoader cl = ActionBar.class.getClassLoader();
      Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component", false, cl);

      // Component.text(String)
      Method textFactory = componentClass.getMethod("text", String.class);
      Object component = textFactory.invoke(null, message);

      Method send = player.getClass().getMethod("sendActionBar", componentClass);
      send.invoke(player, component);
      return true;
    } catch (ReflectiveOperationException | LinkageError ignored) {
      return false;
    }
  }

  private static boolean trySpigot(Player player, String message) {
    try {
      Object spigot = player.getClass().getMethod("spigot").invoke(player);

      ClassLoader cl = ActionBar.class.getClassLoader();
      Class<?> chatMessageTypeClass =
          Class.forName("net.md_5.bungee.api.ChatMessageType", false, cl);
      Class<?> baseComponentClass =
          Class.forName("net.md_5.bungee.api.chat.BaseComponent", false, cl);
      Class<?> textComponentClass =
          Class.forName("net.md_5.bungee.api.chat.TextComponent", false, cl);

      Object actionBarEnum = enumConstant(chatMessageTypeClass, "ACTION_BAR");
      if (actionBarEnum == null) {
        return false;
      }

      // TextComponent.fromLegacyText(String) -> BaseComponent[]
      Method fromLegacyText = textComponentClass.getMethod("fromLegacyText", String.class);
      Object baseComponentArray = fromLegacyText.invoke(null, message);
      if (baseComponentArray == null || !baseComponentArray.getClass().isArray()) {
        return false;
      }

      // sendMessage(ChatMessageType, BaseComponent...)
      Method sendMessage = findSendMessage(spigot.getClass(), chatMessageTypeClass, baseComponentClass);
      if (sendMessage == null) {
        return false;
      }

      // Ensure parameter is BaseComponent[] with correct component type
      Object coerced = coerceArray(baseComponentArray, baseComponentClass);
      sendMessage.invoke(spigot, actionBarEnum, coerced);
      return true;
    } catch (ReflectiveOperationException | LinkageError e) {
      // Don't spam logs every tick; only log at fine level.
      String n = e.getClass().getSimpleName();
      if (player.getServer().getLogger().isLoggable(Level.FINE)) {
        player.getServer().getLogger().fine("ActionBar spigot send failed: " + n);
      }
      return false;
    }
  }

  private static Object enumConstant(Class<?> enumClass, String name) {
    Object[] constants = enumClass.getEnumConstants();
    if (constants == null) {
      return null;
    }
    for (Object c : constants) {
      if (c instanceof Enum<?> && ((Enum<?>) c).name().equals(name)) {
        return c;
      }
    }
    return null;
  }

  private static Method findSendMessage(Class<?> spigotClass, Class<?> chatMessageType, Class<?> baseComponent)
      throws ReflectiveOperationException {
    // Most common: sendMessage(ChatMessageType, BaseComponent[])
    try {
      return spigotClass.getMethod("sendMessage", chatMessageType, Array.newInstance(baseComponent, 0).getClass());
    } catch (NoSuchMethodException ignored) {
      // Some versions: sendMessage(ChatMessageType, BaseComponent...)
      for (Method m : spigotClass.getMethods()) {
        if (!m.getName().equals("sendMessage")) {
          continue;
        }
        Class<?>[] p = m.getParameterTypes();
        if (p.length == 2 && p[0].equals(chatMessageType) && p[1].isArray()) {
          return m;
        }
      }
      return null;
    }
  }

  private static Object coerceArray(Object array, Class<?> componentType) {
    int len = Array.getLength(array);
    Object out = Array.newInstance(componentType, len);
    for (int i = 0; i < len; i++) {
      Array.set(out, i, Array.get(array, i));
    }
    return out;
  }
}
