package me.dw1e.axiom.misc;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.check.api.handler.TickHandler;
import me.dw1e.axiom.config.ConfigValue;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.util.TaskUtil;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

public final class ServerTickTask implements Runnable {

    private BukkitTask task;

    private int tick;
    private long lastRespond;
    private long lastAlert;

    public ServerTickTask() {
        long now = System.currentTimeMillis();

        lastRespond = now;
        lastAlert = now;
    }

    public void enable() {
        task = TaskUtil.runTaskTimer(this, 1L);
    }

    public void disable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public void run() {
        ++tick;

        long now = System.currentTimeMillis();
        long delay = now - lastRespond;
        lastRespond = now;

        if (Axiom.getPlugin().getDataManager() == null) return;
        Collection<PlayerData> allData = Axiom.getPlugin().getDataManager().getAllData();

        boolean refreshBypass = tick % 100 == 0;

        for (PlayerData data : allData) {
            data.getPingProcessor().testConnect();

            // 缓存绕过权限
            if (refreshBypass) data.setBypass(data.getPlayer().hasPermission("axiom.bypass"));

            for (TickHandler tickCheck : data.getTickChecks()) {
                tickCheck.onTick();
            }
        }

        if (delay > ConfigValue.LAG_PROTECTION_THRESHOLD && now - lastAlert > 15000L) {
            lastAlert = now;

            String message = ConfigValue.LAG_PROTECTION_MESSAGE;
            if (message != null && !message.isEmpty()) {

                message = message.replace("%delay%", String.valueOf(delay));

                for (PlayerData data : allData) {
                    if (!data.isToggleAlert()) continue;

                    data.getPlayer().sendMessage(message);
                }
            }
        }
    }

    public int getTick() {
        return tick;
    }

    public boolean isLagging() {
        return System.currentTimeMillis() - lastRespond > ConfigValue.LAG_PROTECTION_THRESHOLD;
    }
}
