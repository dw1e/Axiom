package me.dw1e.axiom.check.impl.interact;

import com.comphenix.protocol.wrappers.BlockPosition;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.AABB;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.misc.util.BlockUtil;
import me.dw1e.axiom.misc.util.MathUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

// 借鉴自 Hawk 作者 Islandscout
public final class InteractB extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.INTERACT, "B", "检查是否看向放置的方块")
                    .setThresholds(5);

    public InteractB(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketBlockPlace) {
            CPacketBlockPlace wrapper = (CPacketBlockPlace) packet;

            if (!wrapper.isPlacedBlock()) return;

            BlockPosition blockPos = wrapper.getBlockPosition();
            Vector blockFace = wrapper.getBlockFaceNormal();

            PlayerLocation from = moveProcessor.getFrom();
            PlayerLocation to = moveProcessor.getTo();

            double eyeHeight = actionProcessor.isSneaking() ? 1.54 : 1.62;

            Vector eyePos = new Vector(from.getX(), from.getY() + eyeHeight, from.getZ());
            Vector direction = MathUtil.getDirection(to.getYaw(), to.getPitch());

            Block targetBlock = BlockUtil.getBlockAsync(blockPos.toLocation(data.getPlayer().getWorld()));
            if (targetBlock != null) {
                AABB hitbox = VISITOR.getBlockBox(targetBlock);

                if (direction.dot(blockFace) >= 0 && !hitbox.containsPoint(eyePos)) {

                    if (flag()) {
                        packet.setCancel(true);

                        BlockPosition resyncPos = blockPos.add(new BlockPosition(wrapper.getBlockFaceNormal()));
                        BlockUtil.resyncBlockAt(data.getPlayer(), resyncPos);
                    }
                }
            }
        }
    }
}