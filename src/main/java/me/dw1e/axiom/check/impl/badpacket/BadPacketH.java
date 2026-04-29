package me.dw1e.axiom.check.impl.badpacket;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketHeldItemSlot;
import me.dw1e.axiom.packet.wrapper.server.SPacketHeldItemSlot;

public final class BadPacketH extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "H", "检查发送相同的切换物品栏位数据包")
                    .setThresholds(3);

    private int lastSlot = -1;
    private boolean server;

    public BadPacketH(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketHeldItemSlot && data.getTick() > 40) {
            int slot = ((CPacketHeldItemSlot) packet).getSlot();

            if (slot == lastSlot && !server) flag("slot=" + slot);

            lastSlot = slot;
            server = false;

        } else if (packet instanceof SPacketHeldItemSlot) {
            server = true;
        }
    }
}
