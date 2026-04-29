package me.dw1e.axiom.check.impl.killaura;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.client.CPacketUseEntity;

public final class KillAuraF extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.KILL_AURA, "F", "检查无效的数据包顺序 (Auto Block)")
                    .setThresholds(3);

    private boolean attacked, interacted;

    public KillAuraF(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            attacked = interacted = false;

        } else if (packet instanceof CPacketUseEntity) {
            CPacketUseEntity wrapper = (CPacketUseEntity) packet;

            if (wrapper.getAction() == EnumWrappers.EntityUseAction.ATTACK) {
                attacked = true;
            }

            if (wrapper.getAction() == EnumWrappers.EntityUseAction.INTERACT
                    || wrapper.getAction() == EnumWrappers.EntityUseAction.INTERACT_AT) {
                interacted = true;
            }

        } else if (packet instanceof CPacketBlockPlace) {
            if (attacked && !interacted) flag();
        }
    }
}
