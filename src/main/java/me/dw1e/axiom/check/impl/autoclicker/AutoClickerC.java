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

public final class AutoClickerC extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.AUTO_CLICKER, "C", "检查点击的平均值")
                    .setVLToAlert(2)
                    .setThresholds(10);

    private final Queue<Integer> averageTicks = new ArrayDeque<>();

    private boolean swung;
    private int ticks, lastTicks, done;

    public AutoClickerC(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            if (swung && !actionProcessor.isDigging()) {
                if (ticks < 7) {
                    averageTicks.add(ticks);

                    if (averageTicks.size() > 50) averageTicks.poll();
                }

                if (averageTicks.size() > 40) {
                    double average = MathUtil.mean(averageTicks);

                    if (average < 2.5) {
                        if (ticks > 3 && ticks < 20 && lastTicks < 20) {
                            decreaseVL(0.25);
                            done = 0;

                        } else if (done++ > 600.0 / (average * 1.5)) {
                            flag(String.format("avg=%.5f", average));

                            done = 0;
                        }
                    } else done = 0;
                }

                lastTicks = ticks;
                ticks = 0;
            }

            swung = false;
            ++ticks;

        } else if (packet instanceof CPacketArmAnimation) {
            swung = true;
        }
    }
}
