package me.dw1e.axiom.check.impl.timer;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.util.MathUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

import java.util.ArrayDeque;
import java.util.Deque;

public final class TimerB extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.TIMER, "B", "通过数据包平均发送间隔检测客户端 Tick 加速")
                    .setVLToAlert(3);

    private final Deque<Long> samples = new ArrayDeque<>();
    private Long lastTimestamp;

    public TimerB(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            long timestamp = System.currentTimeMillis();

            if (lastTimestamp != null) {

                if (data.getTick() < 40
                        || moveProcessor.getTicksSinceTeleport() < 4
                        || actionProcessor.isInsideVehicle()) {
                    samples.clear();
                    return;
                }

                long delay = timestamp - lastTimestamp;
                if (delay >= 5L) samples.add(delay);

                if (samples.size() >= 20) {
                    double mean = MathUtil.mean(samples);
                    double speed = 50.0 / mean;

                    if (mean <= 49.5) {
                        flag(String.format("speed=%.2fx", speed));
                    } else {
                        decreaseVL(0.4);
                    }

                    samples.clear();
                }
            }

            lastTimestamp = timestamp;
        }
    }
}
