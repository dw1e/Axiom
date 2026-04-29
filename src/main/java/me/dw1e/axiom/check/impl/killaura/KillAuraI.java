package me.dw1e.axiom.check.impl.killaura;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.client.CPacketUseEntity;

public final class KillAuraI extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.KILL_AURA, "I", "检查同时攻击多个目标")
                    .setThresholds(1);

    private int count;
    private int lastEntityId;

    public KillAuraI(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            count = 0;

        } else if (packet instanceof CPacketUseEntity) {
            CPacketUseEntity wrapper = ((CPacketUseEntity) packet);

            if (wrapper.getAction() == EnumWrappers.EntityUseAction.ATTACK) {
                int entityId = wrapper.getEntityId();

                if (entityId != lastEntityId && ++count > 1) flag("count=" + count);

                lastEntityId = entityId;
            }
        }
    }
}
