package me.dw1e.axiom.check.impl.fly;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class FlyD extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.FLY, "D", "检查攀爬速度")
                    .setVLToAlert(3)
                    .setThresholds(15);

    public FlyD(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying && ((CPacketFlying) packet).isPosition()) {
            if (!collideProcessor.isClimbing()
                    || collideProcessor.isStep()
                    || collideProcessor.getTicksSincePushedByPiston() < 2
                    || attributeProcessor.isFlying()
                    || moveProcessor.getTicksSinceVelocity() <= moveProcessor.getMaxVelocityTicks()) return;

            float deltaY = (float) moveProcessor.getDeltaY();

            if (deltaY != 0.0F) {
                if (deltaY > 0.0F) {
                    float limit = 0.1176F;

                    int airTicks = moveProcessor.getAirTicks();
                    if (airTicks <= 4 + attributeProcessor.getJumpEffect()) {
                        limit = attributeProcessor.getAttributeJump();
                    }

                    if (collideProcessor.getTicksSinceInWater() < 3) limit = 0.3F;

                    if (deltaY > limit) {
                        flag(String.format("↑ deltaY=%s/%s, ticks=%s", deltaY, limit, airTicks),
                                Math.max(1.0, (deltaY - limit) * 5.0));
                    }

                } else {
                    float limit = -0.15F;

                    if (collideProcessor.getClimbTicks() < 3) {
                        limit = (float) Math.min(moveProcessor.getLastDeltaY() * 2.0F, -0.15F);
                    }

                    if (collideProcessor.isInWater()) limit -= 0.0755F;

                    if (deltaY < limit) {
                        flag(String.format("↓ deltaY=%s/%s", deltaY, limit),
                                Math.max(1.0, (limit - deltaY) * 5.0));
                    }
                }
            }
        }
    }
}
