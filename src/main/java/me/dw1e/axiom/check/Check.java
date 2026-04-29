package me.dw1e.axiom.check;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.api.event.AxiomFlagEvent;
import me.dw1e.axiom.check.manager.AlertManager;
import me.dw1e.axiom.check.manager.PunishManager;
import me.dw1e.axiom.config.CheckValue;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.data.processor.impl.*;
import me.dw1e.axiom.misc.ServerTickTask;
import me.dw1e.axiom.nms.NMSVisitor;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import org.bukkit.Bukkit;

public abstract class Check {

    protected static final NMSVisitor VISITOR = Axiom.getPlugin().getNmsManager().getVisitor();
    protected static final ServerTickTask SERVER_TICK_TASK = Axiom.getPlugin().getServerTickTask();

    protected final PlayerData data;
    private final CheckMeta checkMeta;

    protected ActionProcessor actionProcessor;
    protected AttributeProcessor attributeProcessor;
    protected CollideProcessor collideProcessor;
    protected EmulationProcessor emulationProcessor;
    protected EntityProcessor entityProcessor;
    protected MoveProcessor moveProcessor;
    protected PingProcessor pingProcessor;

    protected double violations;

    public Check(PlayerData data, CheckMeta checkMeta) {
        this.data = data;
        this.checkMeta = checkMeta;

        // GUI 刚开始渲染时需要加载一遍所有检测, 不这样会炸
        if (data != null) {
            actionProcessor = data.getActionProcessor();
            attributeProcessor = data.getAttributeProcessor();
            collideProcessor = data.getCollideProcessor();
            emulationProcessor = data.getEmulationProcessor();
            entityProcessor = data.getEntityProcessor();
            moveProcessor = data.getMoveProcessor();
            pingProcessor = data.getPingProcessor();
        }
    }

    public abstract void handle(WrappedPacket packet);

    public boolean flag() {
        return flag("", 1);
    }

    public boolean flag(String debug) {
        return flag(debug, 1);
    }

    public boolean flag(double addVL) {
        return flag("", addVL);
    }

    public boolean flag(String debug, double addVL) {
        CheckValue checkValue = Axiom.getPlugin().getCheckManager().getCheckValue(getClass());
        if (!checkValue.isEnabled()) return false;

        AxiomFlagEvent flagEvent = new AxiomFlagEvent(data.getPlayer(), checkMeta, addVL, violations, debug);
        Bukkit.getPluginManager().callEvent(flagEvent);

        if (flagEvent.isCancelled()) return false;

        violations += addVL;

        if (violations >= checkMeta.getVLToAlert()) {
            AlertManager.handleAlert(data, checkMeta, checkValue, violations, debug);
        }
        if (violations >= checkMeta.getThresholds()) {
            PunishManager.handlePunish(data, checkMeta, checkValue);
        }

        return true;
    }

    public void decreaseVL() {
        decreaseVL(1);
    }

    public void decreaseVL(double amount) {
        violations -= Math.min(violations, amount);
    }

    public CheckMeta getCheckInfo() {
        return checkMeta;
    }

    public double getViolations() {
        return violations;
    }

    public void resetVL() {
        violations = 0.0;
    }
}
