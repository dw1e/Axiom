package me.dw1e.axiom.check.impl.badpacket;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class BadPacketN extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "N", "检查垂直方向视角超过 90 度")
                    .setThresholds(1);

    public BadPacketN(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            CPacketFlying wrapper = (CPacketFlying) packet;

            if (!wrapper.isRotation()) return;

            float pitch = wrapper.getPitch();

            if (Math.abs(pitch) > 90.0F) flag("pitch=" + pitch);
        }
    }
}
