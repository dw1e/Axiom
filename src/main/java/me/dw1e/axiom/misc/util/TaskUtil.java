package me.dw1e.axiom.misc.util;

import me.dw1e.axiom.Axiom;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public final class TaskUtil {

    public static BukkitTask runTask(Runnable runnable) {
        return Bukkit.getScheduler().runTask(Axiom.getPlugin(), runnable);
    }

    public static BukkitTask runTaskLater(Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskLater(Axiom.getPlugin(), runnable, delay);
    }

    public static BukkitTask runTaskTimer(Runnable runnable, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimer(Axiom.getPlugin(), runnable, delay, period);
    }

    public static BukkitTask runTaskTimer(Runnable runnable, long period) {
        return runTaskTimer(runnable, period, period);
    }
}
