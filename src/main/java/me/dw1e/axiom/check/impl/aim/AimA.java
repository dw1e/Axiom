package me.dw1e.axiom.check.impl.aim;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.Buffer;
import me.dw1e.axiom.misc.util.MathUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class AimA extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.AIM, "A", "检查异常视角步进");

    private final Buffer buffer = new Buffer(10);

    public AimA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying && ((CPacketFlying) packet).isRotation()
                && actionProcessor.getTicksSinceAttack() < 5) {

            float deltaYaw = moveProcessor.getDeltaYaw();
            float deltaPitch = moveProcessor.getDeltaPitch();

            if (deltaYaw < 1.0F || deltaPitch < 1.0F) return;

            float lastDeltaPitch = moveProcessor.getLastDeltaPitch();
            double divisor = MathUtil.gcd(deltaPitch, lastDeltaPitch);

            if (divisor < 0.0078125F) {
                if (buffer.add() > 8) {
                    flag(String.format("d=%.7f", divisor));
                }
            } else {
                buffer.reduce(0.25);
            }
        }
    }
}
