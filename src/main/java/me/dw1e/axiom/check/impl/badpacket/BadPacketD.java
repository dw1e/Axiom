package me.dw1e.axiom.check.impl.badpacket;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketUseEntity;

public final class BadPacketD extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "D", "检查自己与自己交互")
                    .setThresholds(1);

    public BadPacketD(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketUseEntity
                && ((CPacketUseEntity) packet).getEntityId() == data.getPlayer().getEntityId()) {

            if (flag()) packet.setCancel(true);
        }
    }
}
