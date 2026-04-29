package me.dw1e.axiom.check.impl.interact;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.AABB;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.misc.Ray;
import me.dw1e.axiom.misc.util.BlockUtil;
import me.dw1e.axiom.misc.util.MathUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockDig;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public final class InteractD extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.INTERACT, "D", "检查方块交互距离修改")
                    .setThresholds(8);

    private BlockPosition blockPos;
    private String type;

    public InteractD(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketBlockDig) {
            CPacketBlockDig wrapper = (CPacketBlockDig) packet;

            if (wrapper.getPlayerDigType() == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
                blockPos = wrapper.getBlockPosition();
                type = "dig";
            }

        } else if (packet instanceof CPacketBlockPlace) {
            blockPos = ((CPacketBlockPlace) packet).getBlockPosition();
            type = "place";

        } else if (packet instanceof CPacketFlying) {
            BlockPosition blockPos = this.blockPos;
            String type = this.type;

            this.blockPos = null;
            this.type = null;

            if (blockPos == null) return;

            World world = data.getPlayer().getWorld();
            Block targetBlock = BlockUtil.getBlockAsync(blockPos.toLocation(world));

            if (targetBlock == null) return;

            // 乘坐载具时不会发送位置更新包, 不检查
            if (actionProcessor.isInsideVehicle()) return;

            PlayerLocation from = moveProcessor.getFrom();
            PlayerLocation to = moveProcessor.getTo();

            double eyeHeight = actionProcessor.wasSneak() ? 1.54 : 1.62;

            Vector eyePos = new Vector(from.getX(), from.getY() + eyeHeight, from.getZ());
            Vector direction = MathUtil.getDirection(to.getYaw(), to.getPitch());
            Ray ray = new Ray(eyePos, direction);

            double closestDistance = Double.MAX_VALUE;
            Vector intersection = null;

            for (AABB box : VISITOR.getBlockBoxes(targetBlock)) {
                Vector hit = box.intersectsRay(ray, 0.0F, 8);

                if (hit != null) {
                    double dist = hit.distance(eyePos);

                    if (dist < closestDistance) {
                        closestDistance = dist;
                        intersection = hit;
                    }
                }
            }

            String targetBlockName = targetBlock.getType().name().toLowerCase();

            if (intersection != null) {
                double maxDistance = attributeProcessor.isInstantlyBuild() ? 5.0 : 4.5;

                if (closestDistance > maxDistance) {
                    flag(String.format("%s %s at %.5f blocks", type, targetBlockName, closestDistance));
                }

                BlockIterator iterator = new BlockIterator(world,
                        eyePos, direction, 0.0, (int) Math.ceil(closestDistance));

                while (iterator.hasNext()) {
                    Block block = iterator.next();

                    if (block.equals(targetBlock)) break;
                    if (block.isEmpty() || block.isLiquid()) continue;

                    for (AABB blockBox : VISITOR.getBlockBoxes(block)) {
                        Vector blockHit = blockBox.intersectsRay(ray, 0.0F, (float) closestDistance);

                        if (blockHit != null && blockHit.distance(eyePos) < closestDistance) {
                            String blockName = block.getType().name().toLowerCase();

                            flag(String.format("%s %s through %s", type, targetBlockName, blockName));
                            return;
                        }
                    }
                }
            }

        }
    }
}
