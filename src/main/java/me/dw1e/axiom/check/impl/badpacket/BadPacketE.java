package me.dw1e.axiom.check.impl.badpacket;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.client.CPacketHeldItemSlot;

public final class BadPacketE extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "E", "检查同时发送多个切换物品栏位的数据包")
                    .setThresholds(3);

    private int count;

    public BadPacketE(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketHeldItemSlot) {
            ++count;

        } else if (packet instanceof CPacketFlying) {
            if (count > 1) flag("count=" + count);

            count = 0;
        }
    }
}
