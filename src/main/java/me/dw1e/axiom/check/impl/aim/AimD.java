package me.dw1e.axiom.check.impl.aim;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.Buffer;
import me.dw1e.axiom.misc.evicting.EvictingList;
import me.dw1e.axiom.misc.util.MathUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class AimD extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.AIM, "D", "检查极其平滑的转头")
                    .setThresholds(10);

    private final EvictingList<Float> yawAccelSamples = new EvictingList<>(20);
    private final EvictingList<Float> pitchAccelSamples = new EvictingList<>(20);

    private final Buffer buffer = new Buffer(20);

    public AimD(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying && ((CPacketFlying) packet).isRotation()) {
            float yawAccel = moveProcessor.getAccelYaw();
            float pitchAccel = moveProcessor.getAccelPitch();

            yawAccelSamples.add(yawAccel);
            pitchAccelSamples.add(pitchAccel);

            if (yawAccelSamples.isFull() && pitchAccelSamples.isFull()) {
                double yawAccelAVG = MathUtil.mean(yawAccelSamples);
                double pitchAccelAVG = MathUtil.mean(pitchAccelSamples);

                double yawAccelDEV = MathUtil.deviation(yawAccelSamples);
                double pitchAccelDEV = MathUtil.deviation(pitchAccelSamples);

                boolean exemptRotation = moveProcessor.getDeltaYaw() < 1.5F;

                boolean averageInvalid = yawAccelAVG < 1 || pitchAccelAVG < 1 && !exemptRotation;
                boolean deviationInvalid = yawAccelDEV < 5 && pitchAccelDEV > 5 && !exemptRotation;

                if (averageInvalid && deviationInvalid) {
                    if (buffer.add() > 8) {
                        flag(String.format(
                                "y1=%.2f, p1=%.2f, y2=%.2f, p2=%.2f",
                                yawAccelAVG, pitchAccelAVG, yawAccelDEV, pitchAccelDEV
                        ));
                    }
                } else {
                    buffer.reduce(0.75);
                }
            }
        }
    }
}
