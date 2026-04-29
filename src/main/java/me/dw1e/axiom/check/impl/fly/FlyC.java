package me.dw1e.axiom.check.impl.fly;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class FlyC extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.FLY, "C", "检查相同的垂直方向运动")
                    .setVLToAlert(3);

    private int streaks;

    public FlyC(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying && ((CPacketFlying) packet).isPosition()) {
            if (data.getTick() < 20
                    || attributeProcessor.isFlying()
                    || actionProcessor.isInsideVehicle()
                    || collideProcessor.isStep()
                    || collideProcessor.isClimbing()
                    || collideProcessor.getTicksSinceInWater() < 2
                    || collideProcessor.getTicksSinceInLava() < 2
                    || collideProcessor.getTicksSincePushedByPiston() < 2
            ) return;

            double deltaY = moveProcessor.getDeltaY();
            double lastDeltaY = moveProcessor.getLastDeltaY();

            if (deltaY == 0.0) return;

            if (deltaY == lastDeltaY) {
                flag(String.format("+ deltaY=%.5f, streaks=%s", deltaY, ++streaks), Math.min(streaks, 5));

            } else if (deltaY == -lastDeltaY) {
                if (deltaY <= -3.92F
                        || collideProcessor.isOnSlime()
                        || collideProcessor.isInWeb()
                        || collideProcessor.getTicksSinceUnderBlock() < 2
                ) return;

                flag(String.format("- deltaY=%.5f, streaks=%s", deltaY, ++streaks), Math.min(streaks, 5));

            } else {
                streaks = 0;
            }
        }
    }
}
