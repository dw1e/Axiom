package me.dw1e.axiom.check.impl.interact;

import com.comphenix.protocol.wrappers.BlockPosition;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.AABB;
import me.dw1e.axiom.misc.util.BlockUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import org.bukkit.block.Block;

public final class InteractE extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.INTERACT, "E", "检查在空中放置方块")
                    .setThresholds(10);

    private static final int[][] FACES = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    public InteractE(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketBlockPlace) {
            CPacketBlockPlace wrapper = (CPacketBlockPlace) packet;

            if (!wrapper.isPlacedBlock()) return;

            BlockPosition placed = wrapper.getBlockPosition().add(new BlockPosition(wrapper.getBlockFaceNormal()));

            if (!isConnected(placed)) {
                if (flag()) {
                    packet.setCancel(true);
                    BlockUtil.resyncBlockAt(data.getPlayer(), placed);
                }
            }
        }
    }

    private boolean isConnected(BlockPosition pos) {
        for (int[] face : FACES) {
            BlockPosition nearby = new BlockPosition(
                    pos.getX() + face[0],
                    pos.getY() + face[1],
                    pos.getZ() + face[2]
            );

            if (collideProcessor.getGhostBlocks().contains(nearby) || isSolidBlock(nearby)) return true;
        }

        return false;
    }

    private boolean isSolidBlock(BlockPosition pos) {
        AABB box = BlockUtil.getFullBlockAABB(pos);

        for (Block block : box.getBlocks(data.getPlayer().getWorld())) {
            if (!block.isEmpty() && !block.isLiquid()) return true;
        }

        return false;
    }
}