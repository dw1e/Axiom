package me.dw1e.axiom.check.impl.badpacket;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketKeepAlive;

public final class BadPacketI extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "I", "检查发送相同的心跳包 ID")
                    .setThresholds(3);

    private int lastId = -1;

    public BadPacketI(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketKeepAlive && data.getTick() > 40) {
            int id = ((CPacketKeepAlive) packet).getId();

            if (id == lastId) flag("id=" + id);

            lastId = id;
        }
    }
}
