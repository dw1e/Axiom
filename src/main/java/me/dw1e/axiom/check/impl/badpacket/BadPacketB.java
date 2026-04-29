package me.dw1e.axiom.check.impl.badpacket;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class BadPacketB extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "B", "检查超过 20 Ticks 未更新位置")
                    .setThresholds(3);

    private int streaks;

    public BadPacketB(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            if (((CPacketFlying) packet).isPosition() || actionProcessor.isInsideVehicle()) {
                streaks = 0;
                return;
            }

            if (++streaks > 20) flag("s=" + streaks, streaks > 22 ? 1.0 : 0.1);
        }
    }
}
