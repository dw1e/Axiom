package me.dw1e.axiom.check.impl.velocity;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class VelocityA extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.VELOCITY, "A", "检查垂直方向的击退距离修改");

    private double predictedY;

    public VelocityA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            int ticks = moveProcessor.getTicksSinceVelocity();
            int maxTicks = 3;

            if (ticks == 0) predictedY = moveProcessor.getVelocityY();

            boolean jumped = collideProcessor.isJump();
            float attributeJump = attributeProcessor.getAttributeJump();

            if (jumped) predictedY = attributeJump;

            if (ticks > maxTicks
                    || attributeProcessor.isFlying()
                    || collideProcessor.isUnderBlock()
                    || actionProcessor.isInsideVehicle()
                    || moveProcessor.getTicksSinceTeleport() == 0
            ) predictedY = 0.0;

            if (predictedY > 0.0) {
                if (collideProcessor.isInWeb()) predictedY *= 0.05;

                if (predictedY < 0.005) predictedY = 0.0;

                double deltaY = moveProcessor.getDeltaY();
                double offset = predictedY - deltaY;

                boolean offsetMotion = moveProcessor.isOffsetMotion();
                double threshold = offsetMotion ? 0.03 : 1E-5;

                if (collideProcessor.isInWeb()) threshold += 0.023;

                if (offset > threshold) {
                    flag(String.format("o=%.5f (%.5f/%.5f), t=%s/%s", offset, deltaY, predictedY, ticks, maxTicks));
                } else {
                    violations *= 0.99;
                }

                if (collideProcessor.isInWater()) {
                    predictedY *= 0.8F;
                    predictedY -= 0.02;

                } else if (collideProcessor.isInLava()) {
                    predictedY *= 0.5F;
                    predictedY -= 0.02;

                } else {
                    predictedY -= 0.08;
                    predictedY *= 0.98F;
                }
            }
        }
    }
}
