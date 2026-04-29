package me.dw1e.axiom.check.impl.badpacket;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockDig;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class BadPacketG extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "G", "检查同时发送多个释放使用物品的数据包")
                    .setThresholds(3);

    private int count;

    public BadPacketG(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketBlockDig
                && ((CPacketBlockDig) packet).getPlayerDigType() == EnumWrappers.PlayerDigType.RELEASE_USE_ITEM) {

            if (++count > 1) flag("count=" + count);

        } else if (packet instanceof CPacketFlying) {
            count = 0;
        }
    }
}
