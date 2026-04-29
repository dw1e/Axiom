package me.dw1e.axiom.check.impl.badpacket;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.util.BlockUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockDig;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class BadPacketM extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "M", "检查快速挖掘的特征")
                    .setThresholds(3);

    private int stage;

    public BadPacketM(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            if (stage == 1) {
                stage = 2;
            } else {
                stage = 0;
            }

        } else if (packet instanceof CPacketBlockDig) {
            CPacketBlockDig wrapper = (CPacketBlockDig) packet;

            EnumWrappers.PlayerDigType digType = wrapper.getPlayerDigType();

            if (digType == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK) {
                stage = 1;

            } else if (digType == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
                if (stage == 2) {
                    if (flag()) {
                        packet.setCancel(true);
                        BlockUtil.resyncBlockAt(data.getPlayer(), wrapper.getBlockPosition());
                    }
                }

                stage = 0;
            }
        }
    }

}
