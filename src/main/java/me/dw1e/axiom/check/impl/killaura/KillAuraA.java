package me.dw1e.axiom.check.impl.killaura;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketArmAnimation;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.client.CPacketUseEntity;

public final class KillAuraA extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.KILL_AURA, "A", "检查在攻击时不挥手")
                    .setThresholds(3);

    private boolean swung, attacked;

    public KillAuraA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            if (!swung && attacked) flag();

            swung = attacked = false;

        } else if (packet instanceof CPacketArmAnimation) {
            swung = true;

        } else if (packet instanceof CPacketUseEntity
                && ((CPacketUseEntity) packet).getAction() == EnumWrappers.EntityUseAction.ATTACK) {
            attacked = true;
        }
    }
}
