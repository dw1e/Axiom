package me.dw1e.axiom.check.impl.aim;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class AimE extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.AIM, "E", "检查异常机械的瞄准定位")
                    .setVLToAlert(3)
                    .setThresholds(10);

    public AimE(PlayerData data) {
        super(data, CHECK_META);
    }

    private static boolean isLargeMove(float now, float last, float lastLast) {
        return now == 0.0F && last >= 20.0F && lastLast == 0.0F;
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            if (data.getTick() < 20 || moveProcessor.getTicksSinceTeleport() < 3 || pingProcessor.hasLag()) return;

            float lastLastDeltaYaw = moveProcessor.getLastLastDeltaYaw();
            float lastLastDeltaPitch = moveProcessor.getLastLastDeltaPitch();

            float lastDeltaYaw = moveProcessor.getLastDeltaYaw();
            float lastDeltaPitch = moveProcessor.getLastDeltaPitch();

            float deltaYaw = moveProcessor.getDeltaYaw();
            float deltaPitch = moveProcessor.getDeltaPitch();

            if (isLargeMove(deltaYaw, lastDeltaYaw, lastLastDeltaYaw)) {
                flag(String.format("yaw=%.2f", lastDeltaYaw));
            }

            if (isLargeMove(deltaPitch, lastDeltaPitch, lastLastDeltaPitch)
                    && Math.abs(moveProcessor.getPrevious().getPitch()) != 90.0F
                    && Math.abs(moveProcessor.getTo().getPitch()) != 90.0F) {

                flag(String.format("pitch=%.2f", lastDeltaPitch));
            }
        }
    }
}
