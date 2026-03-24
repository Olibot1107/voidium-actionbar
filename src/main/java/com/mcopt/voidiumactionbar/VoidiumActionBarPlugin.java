package com.mcopt.voidiumactionbar;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class VoidiumActionBarPlugin extends JavaPlugin implements Listener {
  private static final String MESSAGE = "Running on Voidium Hosting";
  private static final int INTERVAL_TICKS = 20;

  private BukkitTask task;

  @Override
  public void onEnable() {
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
    ActionBar.send(event.getPlayer(), MESSAGE);
  }

  private void startLoop() {
    if (task != null) {
      task.cancel();
    }

    task =
        Bukkit.getScheduler()
            .runTaskTimer(
                this,
                () -> Bukkit.getOnlinePlayers().forEach(p -> ActionBar.send(p, MESSAGE)),
                INTERVAL_TICKS,
                INTERVAL_TICKS);
  }
}
