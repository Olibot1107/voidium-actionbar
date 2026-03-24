package com.mcopt.voidiumactionbar;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class VoidiumActionBarPlugin extends JavaPlugin implements Listener {
  private BukkitTask task;

  @Override
  public void onEnable() {
    saveDefaultConfig();
    Bukkit.getPluginManager().registerEvents(this, this);
    startLoop();
  }

  @Override
  public void onDisable() {
    if (task != null) {
      task.cancel();
      task = null;
    }
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    ActionBar.send(event.getPlayer(), getConfig().getString("message", "Running on Voidium Hosting"));
  }

  private void startLoop() {
    if (task != null) {
      task.cancel();
    }

    final String message = getConfig().getString("message", "Running on Voidium Hosting");
    final int intervalTicks = Math.max(1, getConfig().getInt("interval-ticks", 20));

    task =
        Bukkit.getScheduler()
            .runTaskTimer(
                this,
                () -> Bukkit.getOnlinePlayers().forEach(p -> ActionBar.send(p, message)),
                intervalTicks,
                intervalTicks);
  }
}

