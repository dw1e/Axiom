package me.dw1e.axiom.check.impl.badpacket;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.check.api.PunishType;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class BadPacketP extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "P", "检查忽略服务器发送的传送包")
                    .setThresholds(3)
                    .setPunishType(PunishType.KICK)
                    .setPunishReason("Failed teleport confirmation.");

    public BadPacketP(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
    }
}
