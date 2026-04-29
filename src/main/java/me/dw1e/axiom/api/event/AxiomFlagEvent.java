package me.dw1e.axiom.api.event;

import me.dw1e.axiom.check.CheckMeta;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class AxiomFlagEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final CheckMeta checkMeta;
    private final double addVL;
    private final double violations;
    private final String debug;

    private boolean cancelled;

    public AxiomFlagEvent(Player player, CheckMeta checkMeta, double addVL, double violations, String debug) {
        super(!Bukkit.isPrimaryThread()); // 数据包检测都是异步的, 但有一些依赖 Bukkit Event 的检测会跑在主线程

        this.player = player;
        this.checkMeta = checkMeta;
        this.addVL = addVL;
        this.violations = violations;
        this.debug = debug;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }

    public CheckMeta getCheckInfo() {
        return checkMeta;
    }

    public double getAddVL() {
        return addVL;
    }

    public double getViolations() {
        return violations;
    }

    public String getDebug() {
        return debug;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
