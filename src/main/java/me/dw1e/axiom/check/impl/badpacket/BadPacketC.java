package me.dw1e.axiom.check.impl.badpacket;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class BadPacketC extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "C", "检查更新相同的视角位置")
                    .setThresholds(3);

    public BadPacketC(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying && ((CPacketFlying) packet).isRotation()) {
            int ticksSinceTP = moveProcessor.getTicksSinceTeleport();

            if (data.getTick() < 20
                    || ticksSinceTP < 2
                    || actionProcessor.getTicksSinceInsideVehicle() < 4
            ) return;

            PlayerLocation from = moveProcessor.getFrom();
            PlayerLocation to = moveProcessor.getTo();

            if (to.getYaw() == from.getYaw() && to.getPitch() == from.getPitch()) {
                flag("tp=" + (ticksSinceTP < 20 ? ticksSinceTP : "false"));
            }
        }
    }
}
