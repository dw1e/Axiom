package me.dw1e.axiom.check.impl.badpacket;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.check.api.PunishType;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class BadPacketK extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "K", "检查跳过确认数据包")
                    .setThresholds(10)
                    .setPunishType(PunishType.KICK)
                    .setPunishReason("Invalid transaction sequence.");

    public BadPacketK(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
    }
}
