package me.dw1e.axiom.check.impl.badpacket;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.client.CPacketHeldItemSlot;

public final class BadPacketO extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "O", "检查无效的数据包顺序 (Auto Block / Scaffold)")
                    .setThresholds(3);

    private boolean placed;

    public BadPacketO(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            placed = false;

        } else if (packet instanceof CPacketBlockPlace) {
            placed = true;

        } else if (packet instanceof CPacketHeldItemSlot) {
            if (placed) flag();
        }
    }

}