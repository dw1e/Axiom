package me.dw1e.axiom.misc;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.misc.util.BlockUtil;
import me.dw1e.axiom.nms.NMSVisitor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.material.Openable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public final class AdjacentBlocks {

    private static final NMSVisitor VISITOR = Axiom.getPlugin().getNmsManager().getVisitor();

    private static final double[][] BLOCK_SAMPLE_STEPS = {
            {0, 0, 0.3}, {0.3, 0, 0}, {0, 0, -0.3}, {0, 0, -0.3}, {-0.3, 0, 0}, {-0.3, 0, 0}, {0, 0, 0.3}, {0, 0, 0.3}
    };

    private static Set<Block> getBlocksInLocation(Location loc) {
        Location check = loc.clone();
        Set<Block> blocks = new HashSet<>(8);

        for (double[] step : BLOCK_SAMPLE_STEPS) {
            Block block = BlockUtil.getBlockAsync(check.add(step[0], step[1], step[2]));
            if (block != null) blocks.add(block);
        }

        return blocks;
    }

    public static boolean onGroundReally(Location loc, double yVelocity, boolean ignoreInGround, double feetDepth) {
        if (yVelocity > 0.6) return false;

        Location check = loc.clone();

        Set<Block> blocks = new HashSet<>();
        blocks.addAll(AdjacentBlocks.getBlocksInLocation(check));
        blocks.addAll(AdjacentBlocks.getBlocksInLocation(check.clone().add(0, -1, 0)));

        AABB underFeet = new AABB(-0.3, -feetDepth, -0.3, 0.3, 0, 0.3);
        underFeet.shift(loc.toVector());

        for (Block block : blocks) {
            if (block.isLiquid() || !BlockUtil.isReallySolid(block)) continue;

            for (AABB blockBox : VISITOR.getBlockBoxes(block)) {
                if (blockBox.isColliding(underFeet)) {
                    if (ignoreInGround) {
                        AABB topFeet = underFeet.clone();
                        topFeet.shift(new Vector(0, feetDepth + 0.00001, 0));

                        for (Block block1 : AdjacentBlocks.getBlocksInLocation(loc)) {
                            if (block1.isLiquid() || !BlockUtil.isReallySolid(block1)
                                    || block1.getState().getData() instanceof Openable) {
                                continue;
                            }

                            for (AABB blockBox1 : VISITOR.getBlockBoxes(block1)) {
                                if (blockBox1.isColliding(topFeet)) return false;
                            }
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
