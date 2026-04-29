package me.dw1e.axiom.check.impl.scaffold;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class ScaffoldC extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.SCAFFOLD, "C", "检查安全行走的时机")
                    .setThresholds(10);

    private int lastXZMoveTick;
    private int buffer;

    public ScaffoldC(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            if (moveProcessor.getDeltaXZ() > 0.0) {
                lastXZMoveTick = data.getTick();
            }

        } else if (packet instanceof CPacketBlockPlace) {
            CPacketBlockPlace wrapper = (CPacketBlockPlace) packet;
            if (!wrapper.isPlacedBlock()) return;

            int moveDelay = data.getTick() - lastXZMoveTick;

            boolean inAir = !moveProcessor.getTo().isOnGround();
            boolean outsideTimingWindow = moveDelay > 7 || moveDelay <= 3;
            boolean sneaking = actionProcessor.isSneaking();

            if (inAir || outsideTimingWindow || sneaking) {
                buffer = Math.max(0, buffer - 1);
                return;
            }

            if (++buffer >= 3) {
                flag(String.format("delay=%s, buffer=%s", moveDelay, buffer));
                buffer = 0;
            }
        }
    }
}