package me.dw1e.axiom.check.impl.velocity;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class VelocityC extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.VELOCITY, "C", "检查异常的跳跃重置成功率")
                    .setThresholds(5);

    private int success, failure;

    public VelocityC(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            PlayerLocation from = moveProcessor.getFrom();
            PlayerLocation to = moveProcessor.getTo();

            if (moveProcessor.getTicksSinceVelocity() == 1
                    && actionProcessor.isSprinting()
                    && from.isOnGround()
                    && !to.isOnGround()
                    && !collideProcessor.isUnderBlock()
                    && Math.abs(attributeProcessor.getAttributeJump() - moveProcessor.getVelocityY()) > 1E-5
            ) {

                if (collideProcessor.isJump()) {
                    ++success;
                } else {
                    ++failure;
                }

                int total = success + failure;
                if (total >= 10) {

                    double ratio = (double) success / total;
                    if (ratio >= 0.8) {

                        flag(String.format("ratio=%.2f", ratio));
                    }

                    success = failure = 0;
                }

            }

        }
    }
}
