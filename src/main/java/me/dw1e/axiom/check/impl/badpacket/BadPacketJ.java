package me.dw1e.axiom.check.impl.badpacket;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketEntityAction;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class BadPacketJ extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "J", "检查同时发送两个开始行为包")
                    .setThresholds(3);

    private boolean sentSneak, sentSprint;

    public BadPacketJ(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            sentSneak = sentSprint = false;

        } else if (packet instanceof CPacketEntityAction) {
            EnumWrappers.PlayerAction action = ((CPacketEntityAction) packet).getAction();

            if (action == EnumWrappers.PlayerAction.START_SNEAKING) {
                if (sentSneak) flag("sneak");

                sentSneak = true;
            }

            if (action == EnumWrappers.PlayerAction.START_SPRINTING) {
                if (sentSprint) flag("sprint");

                sentSprint = true;
            }
        }
    }
}
