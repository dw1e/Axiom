package me.dw1e.axiom.check.impl.velocity;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class VelocityB extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.VELOCITY, "B", "检查水平方向的击退距离修改")
                    .setVLToAlert(3)
                    .setThresholds(30);

    public VelocityB(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            int ticks = moveProcessor.getTicksSinceVelocity();
            int maxTicks = moveProcessor.getMaxVelocityTicks();

            if (ticks > maxTicks
                    || attributeProcessor.isFlying()
                    || actionProcessor.isInsideVehicle()
                    || collideProcessor.isClimbing()
                    || collideProcessor.isStep()
                    || collideProcessor.isInLava()
                    || moveProcessor.getTicksSinceTeleport() == 0
            ) return;

            double offset = emulationProcessor.getMinDistance();

            boolean offsetMotion = moveProcessor.isOffsetMotion();
            double threshold = offsetMotion ? 0.03 : 1E-7;

            if (collideProcessor.isInWeb()) threshold += 0.0005;

            if (!moveProcessor.getFrom().isUpdatePos() || !moveProcessor.getPrevious().isUpdatePos()) {
                threshold += 1E-4;
            }

            if (offset > threshold) {
                flag(String.format("o=%.7f, t=%s/%s", offset, ticks, maxTicks));

            } else {
                violations *= 0.995;
            }
        }
    }
}
