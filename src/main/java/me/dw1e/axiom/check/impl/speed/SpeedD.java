package me.dw1e.axiom.check.impl.speed;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class SpeedD extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.SPEED, "D", "检查使用物品时不减移速");

    // 这个方法来自     .
    private boolean flaggedLastTick;

    public SpeedD(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying && ((CPacketFlying) packet).isPosition()) {

            if (actionProcessor.isUsingItem() && !emulationProcessor.isUsing()) {
                if (flaggedLastTick) {
                    flag();

                    actionProcessor.resetUseItem();
                }

                flaggedLastTick = true;
            } else {
                violations *= 0.995;

                flaggedLastTick = false;
            }
        }
    }

}
