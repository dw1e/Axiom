package me.dw1e.axiom.check.impl.badpacket;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketHeldItemSlot;

public final class BadPacketF extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "F", "检查无效的物品栏位")
                    .setThresholds(1);

    public BadPacketF(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketHeldItemSlot) {
            int slot = ((CPacketHeldItemSlot) packet).getSlot();

            if (slot < 0 || slot > 8) flag("slot=" + slot);
        }
    }
}
