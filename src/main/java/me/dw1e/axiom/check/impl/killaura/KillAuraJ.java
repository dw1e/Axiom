package me.dw1e.axiom.check.impl.killaura;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.Buffer;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class KillAuraJ extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.KILL_AURA, "J", "检查在攻击时不减移速");

    private final Buffer buffer = new Buffer(5);

    public KillAuraJ(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying && ((CPacketFlying) packet).isPosition()) {
            boolean hasHitSlowdown = actionProcessor.getTicksSinceHitSlowdown() == 0;

            boolean emulationHitSlowdown = emulationProcessor.isHitSlowdown();
            boolean emulationSprint = emulationProcessor.isSprint();

            boolean exempt = attributeProcessor.isFlying();

            if (hasHitSlowdown && !emulationHitSlowdown && emulationSprint && !exempt) {
                if (buffer.add() > 3) flag();

            } else {
                buffer.reduce(0.15);
            }
        }
    }
}
