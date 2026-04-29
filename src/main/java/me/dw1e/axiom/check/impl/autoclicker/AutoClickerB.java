package me.dw1e.axiom.check.impl.autoclicker;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.util.MathUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketArmAnimation;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

import java.util.ArrayDeque;
import java.util.Queue;

public final class AutoClickerB extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.AUTO_CLICKER, "B", "检查点击的偏差")
                    .setVLToAlert(5)
                    .setThresholds(15);

    private final Queue<Integer> intervalTicks = new ArrayDeque<>();

    private boolean swung;
    private int ticks;

    public AutoClickerB(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            if (swung && !actionProcessor.isDigging() && !actionProcessor.isPlacing()) {
                if (ticks < 8) {
                    intervalTicks.add(ticks);

                    if (intervalTicks.size() >= 40) {
                        double deviation = MathUtil.deviation(intervalTicks);

                        violations += Math.max((0.325 - deviation) * 2.0 + 0.675, 0);

                        if (deviation < 0.325) {
                            flag(String.format("dev=%.5f", deviation), 0);
                        }

                        intervalTicks.clear();
                    }
                }

                ticks = 0;
            }

            swung = false;
            ++ticks;

        } else if (packet instanceof CPacketArmAnimation) {
            swung = true;
        }
    }
}
