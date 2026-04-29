package me.dw1e.axiom.check.impl.scaffold;

import com.comphenix.protocol.wrappers.BlockPosition;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class ScaffoldG extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.SCAFFOLD, "G", "检查搭高速度过快");

    private int lastX, lastY, lastZ;
    private int ticks, buffer;

    public ScaffoldG(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketBlockPlace) {
            CPacketBlockPlace wrapper = (CPacketBlockPlace) packet;

            if (!wrapper.isPlacedBlock() || moveProcessor.getDeltaY() <= 0.0 || attributeProcessor.isFlying()) return;

            BlockPosition blockPos = wrapper.getBlockPosition();
            int x = blockPos.getX(), y = blockPos.getY(), z = blockPos.getZ();

            if (lastX == x && y > lastY && lastZ == z) {
                int limit = 7 - attributeProcessor.getJumpEffect();

                if (ticks < limit) {
                    if (++buffer > 2) {
                        flag("ticks=" + ticks + "/" + limit);
                    }

                } else {
                    buffer = 0;
                }

                ticks = 0;
            }

            lastX = x;
            lastY = y;
            lastZ = z;

        } else if (packet instanceof CPacketFlying) {
            ++ticks;
        }
    }
}