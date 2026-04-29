package me.dw1e.axiom.check.manager;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.impl.aim.*;
import me.dw1e.axiom.check.impl.autoclicker.AutoClickerA;
import me.dw1e.axiom.check.impl.autoclicker.AutoClickerB;
import me.dw1e.axiom.check.impl.autoclicker.AutoClickerC;
import me.dw1e.axiom.check.impl.badpacket.*;
import me.dw1e.axiom.check.impl.fly.FlyA;
import me.dw1e.axiom.check.impl.fly.FlyB;
import me.dw1e.axiom.check.impl.fly.FlyC;
import me.dw1e.axiom.check.impl.fly.FlyD;
import me.dw1e.axiom.check.impl.hitbox.HitBoxA;
import me.dw1e.axiom.check.impl.interact.*;
import me.dw1e.axiom.check.impl.inventory.InventoryA;
import me.dw1e.axiom.check.impl.inventory.InventoryB;
import me.dw1e.axiom.check.impl.inventory.InventoryC;
import me.dw1e.axiom.check.impl.killaura.*;
import me.dw1e.axiom.check.impl.netanalysis.NetAnalysisA;
import me.dw1e.axiom.check.impl.netanalysis.NetAnalysisB;
import me.dw1e.axiom.check.impl.phase.PhaseA;
import me.dw1e.axiom.check.impl.scaffold.*;
import me.dw1e.axiom.check.impl.speed.SpeedA;
import me.dw1e.axiom.check.impl.speed.SpeedB;
import me.dw1e.axiom.check.impl.speed.SpeedC;
import me.dw1e.axiom.check.impl.speed.SpeedD;
import me.dw1e.axiom.check.impl.timer.TimerA;
import me.dw1e.axiom.check.impl.timer.TimerB;
import me.dw1e.axiom.check.impl.velocity.VelocityA;
import me.dw1e.axiom.check.impl.velocity.VelocityB;
import me.dw1e.axiom.check.impl.velocity.VelocityC;
import me.dw1e.axiom.config.CheckValue;
import me.dw1e.axiom.config.ConfigValue;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.util.TaskUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class CheckManager {

    private static final List<Function<PlayerData, Check>> FACTORIES = new ArrayList<>();

    static {
        FACTORIES.add(AimA::new);
        FACTORIES.add(AimB::new);
        FACTORIES.add(AimC::new);
        FACTORIES.add(AimD::new);
        FACTORIES.add(AimE::new);

        FACTORIES.add(AutoClickerA::new);
        FACTORIES.add(AutoClickerB::new);
        FACTORIES.add(AutoClickerC::new);

        FACTORIES.add(BadPacketA::new);
        FACTORIES.add(BadPacketB::new);
        FACTORIES.add(BadPacketC::new);
        FACTORIES.add(BadPacketD::new);
        FACTORIES.add(BadPacketE::new);
        FACTORIES.add(BadPacketF::new);
        FACTORIES.add(BadPacketG::new);
        FACTORIES.add(BadPacketH::new);
        FACTORIES.add(BadPacketI::new);
        FACTORIES.add(BadPacketJ::new);
        FACTORIES.add(BadPacketK::new);
        FACTORIES.add(BadPacketL::new);
        FACTORIES.add(BadPacketM::new);
        FACTORIES.add(BadPacketN::new);
        FACTORIES.add(BadPacketO::new);
        FACTORIES.add(BadPacketP::new);

        FACTORIES.add(FlyA::new);
        FACTORIES.add(FlyB::new);
        FACTORIES.add(FlyC::new);
        FACTORIES.add(FlyD::new);

        FACTORIES.add(HitBoxA::new);

        FACTORIES.add(InteractA::new);
        FACTORIES.add(InteractB::new);
        FACTORIES.add(InteractC::new);
        FACTORIES.add(InteractD::new);
        FACTORIES.add(InteractE::new);

        FACTORIES.add(InventoryA::new);
        FACTORIES.add(InventoryB::new);
        FACTORIES.add(InventoryC::new);

        FACTORIES.add(KillAuraA::new);
        FACTORIES.add(KillAuraB::new);
        FACTORIES.add(KillAuraC::new);
        FACTORIES.add(KillAuraD::new);
        FACTORIES.add(KillAuraE::new);
        FACTORIES.add(KillAuraF::new);
        FACTORIES.add(KillAuraG::new);
        FACTORIES.add(KillAuraH::new);
        FACTORIES.add(KillAuraI::new);
        FACTORIES.add(KillAuraJ::new);
        FACTORIES.add(KillAuraK::new);

        FACTORIES.add(NetAnalysisA::new);
        FACTORIES.add(NetAnalysisB::new);

        FACTORIES.add(PhaseA::new);

        FACTORIES.add(ScaffoldA::new);
        FACTORIES.add(ScaffoldB::new);
        FACTORIES.add(ScaffoldC::new);
        FACTORIES.add(ScaffoldD::new);
        FACTORIES.add(ScaffoldE::new);
        FACTORIES.add(ScaffoldF::new);
        FACTORIES.add(ScaffoldG::new);

        FACTORIES.add(SpeedA::new);
        FACTORIES.add(SpeedB::new);
        FACTORIES.add(SpeedC::new);
        FACTORIES.add(SpeedD::new);

        FACTORIES.add(TimerA::new);
        FACTORIES.add(TimerB::new);

        FACTORIES.add(VelocityA::new);
        FACTORIES.add(VelocityB::new);
        FACTORIES.add(VelocityC::new);
    }

    private final Map<Class<? extends Check>, CheckValue> checkValueMap = new HashMap<>();

    private BukkitTask resetVLTask;

    public void enable() {
        for (Check check : loadChecks(null)) {
            checkValueMap.put(check.getClass(), new CheckValue(check.getCheckInfo()));
        }
        Axiom.getPlugin().getConfigManager().saveChecks();

        resetVLTask = startResetVLTask();
    }

    public void disable() {
        if (resetVLTask != null) {
            resetVLTask.cancel();
            resetVLTask = null;
        }

        checkValueMap.clear();
    }

    public List<Check> loadChecks(PlayerData data) {
        List<Check> checks = new ArrayList<>(FACTORIES.size());

        for (Function<PlayerData, Check> factory : FACTORIES) {
            checks.add(factory.apply(data));
        }

        return checks;
    }

    private void resetVL() {
        String message = ConfigValue.VIOLATIONS_RESET_MESSAGE;

        boolean shouldSendMessage = message != null && !message.isEmpty();
        if (shouldSendMessage) {
            message = message.replace("%prefix%", ConfigValue.PREFIX);
        }

        for (PlayerData data : Axiom.getPlugin().getDataManager().getAllData()) {
            data.resetVL();

            if (shouldSendMessage && data.isToggleAlert()) {
                data.getPlayer().sendMessage(message);
            }
        }

        if (shouldSendMessage) Bukkit.getConsoleSender().sendMessage(message);
    }

    private BukkitTask startResetVLTask() {
        long interval = ConfigValue.VIOLATIONS_RESET_INTERVAL * 60L * 20L;

        return TaskUtil.runTaskTimer(this::resetVL, interval);
    }

    public void manualResetVL() {
        if (resetVLTask != null) resetVLTask.cancel();

        resetVL();

        resetVLTask = startResetVLTask();
    }

    public CheckValue getCheckValue(Class<? extends Check> clazz) {
        return checkValueMap.get(clazz);
    }
}
