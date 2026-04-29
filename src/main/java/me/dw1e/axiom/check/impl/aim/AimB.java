package me.dw1e.axiom.check.impl.aim;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class AimB extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.AIM, "B", "检查异常水平方向转头")
                    .setVLToAlert(5)
                    .setThresholds(20);

    private double buffer;

    public AimB(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying && ((CPacketFlying) packet).isRotation()
                && actionProcessor.getTicksSinceAttack() < 5) {

            float pitch = moveProcessor.getTo().getPitch();
            if (Math.abs(pitch) == 90F) return;

            float deltaYaw = moveProcessor.getDeltaYaw();
            float deltaPitch = moveProcessor.getDeltaPitch();

            if (deltaYaw > 0.0F && deltaPitch < 1.0E-4F) {
                buffer += Math.min(deltaYaw, 5.0);

                if (deltaPitch == 0.0F && deltaYaw > 10.0F) buffer += 5.0;

                if (buffer > 50.0) {
                    buffer = 30.0;

                    flag(String.format("dy=%.3f", deltaYaw));
                }
            } else {
                buffer -= Math.min(buffer, 4.0);
            }
        }
    }
}
