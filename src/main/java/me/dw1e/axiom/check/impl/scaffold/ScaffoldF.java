package me.dw1e.axiom.check.impl.scaffold;

import com.comphenix.protocol.wrappers.BlockPosition;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import org.bukkit.block.BlockFace;

public final class ScaffoldF extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.SCAFFOLD, "F", "检查异常的向下搭路")
                    .setThresholds(1);

    public ScaffoldF(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketBlockPlace) {
            CPacketBlockPlace wrapper = (CPacketBlockPlace) packet;

            BlockPosition blockPos = wrapper.getBlockPosition();
            if (blockPos == null) return;

            if (wrapper.getBlockFace() == BlockFace.DOWN) {
                double locY = moveProcessor.getTo().getY();
                int blockY = blockPos.getY();

                if (locY > blockY) flag(String.format("loc=%.5f, block=%s", locY, blockY));
            }
        }
    }
}