package me.dw1e.axiom.check.impl.killaura;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockDig;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.client.CPacketUseEntity;

public final class KillAuraE extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.KILL_AURA, "E", "检查无效的数据包顺序 (Auto Block)")
                    .setThresholds(3);

    private boolean dug;

    public KillAuraE(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            dug = false;

        } else if (packet instanceof CPacketBlockDig) {
            dug = true;

        } else if (packet instanceof CPacketUseEntity) {
            if (dug) flag();
        }
    }
}
