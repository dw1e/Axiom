package me.dw1e.axiom.check.impl.netanalysis;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.check.api.handler.TickHandler;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.util.MathUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

import java.util.ArrayDeque;
import java.util.Deque;

public final class NetAnalysisA extends Check implements TickHandler {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.NET_ANALYSIS, "A", "分析卡顿规律")
                    .setThresholds(8);

    private final Deque<Double> samples = new ArrayDeque<>();

    private int smooth, lag;

    public NetAnalysisA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
    }

    @Override
    public void onTick() {
        if (SERVER_TICK_TASK.isLagging()) return;

        if (pingProcessor.hasLag()) {
            ++lag;
        } else {
            ++smooth;
        }

        if (lag + smooth >= 20) {
            double ratio = (double) lag / (lag + smooth);

            samples.add(ratio);

            lag = smooth = 0;
        }

        if (samples.size() >= 10) {
            double mean = MathUtil.mean(samples);
            double dev = MathUtil.deviation(samples);

            if (mean > 0.1 && mean < 0.9 && dev < 0.1) {
                flag(String.format("mean=%.3f, dev=%.3f", mean, dev));
            }

            samples.clear();
        }
    }
}
