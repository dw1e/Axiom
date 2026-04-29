package me.dw1e.axiom.check.impl.interact;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketArmAnimation;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockDig;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class InteractA extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.INTERACT, "A", "检查在破坏方块时不挥手")
                    .setThresholds(3);

    private boolean dug, swung;

    public InteractA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketArmAnimation) {
            swung = true;

        } else if (packet instanceof CPacketBlockDig
                && ((CPacketBlockDig) packet).getPlayerDigType() == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
            dug = true;

        } else if (packet instanceof CPacketFlying) {
            if (dug && !swung) flag();

            dug = swung = false;
        }
    }
}