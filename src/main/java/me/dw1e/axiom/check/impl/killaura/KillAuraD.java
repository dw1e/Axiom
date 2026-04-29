package me.dw1e.axiom.check.impl.killaura;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.client.CPacketUseEntity;

public final class KillAuraD extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.KILL_AURA, "D", "检查无效的数据包顺序 (Auto Block)")
                    .setThresholds(3);

    private boolean placed;

    public KillAuraD(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            placed = false;

        } else if (packet instanceof CPacketBlockPlace) {
            placed = true;

        } else if (packet instanceof CPacketUseEntity) {
            if (placed) flag();
        }
    }
}
