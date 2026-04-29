package me.dw1e.axiom.check.impl.killaura;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockDig;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.client.CPacketUseEntity;

public final class KillAuraH extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.KILL_AURA, "H", "检查在攻击的同时开始挖掘方块")
                    .setThresholds(3);

    private boolean attacked, dug;

    public KillAuraH(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketUseEntity
                && ((CPacketUseEntity) packet).getAction() == EnumWrappers.EntityUseAction.ATTACK) {
            attacked = true;

        } else if (packet instanceof CPacketBlockDig
                && ((CPacketBlockDig) packet).getPlayerDigType() == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
            dug = true;

        } else if (packet instanceof CPacketFlying) {
            if (attacked && dug) flag();

            attacked = dug = false;
        }
    }
}
