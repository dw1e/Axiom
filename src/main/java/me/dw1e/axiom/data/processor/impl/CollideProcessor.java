package me.dw1e.axiom.data.processor.impl;

import com.comphenix.protocol.wrappers.BlockPosition;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.data.processor.Processor;
import me.dw1e.axiom.misc.AABB;
import me.dw1e.axiom.misc.AdjacentBlocks;
import me.dw1e.axiom.misc.Pair;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.misc.util.BlockUtil;
import me.dw1e.axiom.misc.util.MaterialUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 部分借鉴自 Hawk 作者 Islandscout
public final class CollideProcessor extends Processor {

    private final Map<BlockPosition, Boolean> ghostBlocks = new ConcurrentHashMap<>();

    private List<Block> activeBlocks;

    private boolean wasInWater, wasInLava, inWater, inLava;
    private boolean collideX, collideZ;
    private boolean nearWall;
    private boolean underBlock, onSlime;
    private boolean lastOnGroundRelly, onGroundReally;
    private boolean inWeb;
    private boolean climbing, onEdge;
    private boolean step, jumped;
    private boolean onGhostBlock;

    private List<Pair<Block, Vector>> liquidsAndDirections;
    private Vector waterFlowForce;

    private int ticksSinceNearWall = 100;
    private int ticksSinceOnSlime = 100;
    private int ticksSinceUnderBlock = 100;
    private int ticksSinceInWater = 100;
    private int ticksSinceInLava = 100;
    private int ticksSinceClimb = 100;
    private int ticksSincePushedByPiston = 100;

    private int waterTicks, lavaTicks;
    private int climbTicks;

    public CollideProcessor(PlayerData data) {
        super(data);
    }

    @Override
    public void preProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            PlayerLocation from = data.getMoveProcessor().getFrom();
            PlayerLocation lastPosLoc = data.getMoveProcessor().getLastPosLoc();
            PlayerLocation to = data.getMoveProcessor().getTo();

            World world = data.getPlayer().getWorld();
            Vector toVector = to.toVector();

            double threshold = data.getMoveProcessor().isOffsetMotion()
                    || data.getMoveProcessor().isOffsetYMotion() ? 0.03 : 1E-6;

            // 主要检查水平方向碰撞, 需要即时的XZ轴
            {
                onSlime = collideX = collideZ = nearWall = false;

                AABB playerBox = AABB.PLAYER_COLLISION_BOX.clone().shift(toVector);
                AABB checkXZ = playerBox.clone().expand(1, 1, 1);

                for (Block block : checkXZ.getBlocks(world)) {
                    if (block.isEmpty()) continue;
                    Material mat = block.getType();

                    for (AABB blockBox : VISITOR.getBlockBoxes(block)) {
                        if (playerBox.intersectsY(blockBox)) {
                            if (Math.abs(playerBox.getMaxX() - blockBox.getMinX()) < threshold
                                    || Math.abs(playerBox.getMinX() - blockBox.getMaxX()) < threshold) {
                                collideX = true;
                            }

                            if (Math.abs(playerBox.getMaxZ() - blockBox.getMinZ()) < threshold
                                    || Math.abs(playerBox.getMinZ() - blockBox.getMaxZ()) < threshold) {
                                collideZ = true;
                            }

                            nearWall |= collideX || collideZ;
                        }
                    }
                }
            }

            // 主要检查垂直方向碰撞, 由于Y轴会比XZ轴先更新, 为了精准的碰撞检查, 需要单拎出来
            {
                underBlock = false;

                AABB playerBox = AABB.PLAYER_COLLISION_BOX.clone().shift(from.toVector().setY(to.getY()));
                AABB checkY = playerBox.clone().expand(1, 1, 1);

                for (Block block : checkY.getBlocks(world)) {
                    if (block.isEmpty()) continue;
                    Material mat = block.getType();

                    for (AABB blockBox : VISITOR.getBlockBoxes(block)) {
                        if (playerBox.intersectsXZ(blockBox)) {

                            if (Math.abs(playerBox.getMaxY() - blockBox.getMinY()) < threshold) {
                                underBlock = true;
                            }

                            if (Math.abs(playerBox.getMinY() - blockBox.getMaxY()) < threshold) {
                                onSlime |= mat == Material.SLIME_BLOCK;
                            }
                        }
                    }
                }
            }

            // 检查是否站在客户端放置下来, 但服务器还未接收到的方块上 (幽灵方块)
            {
                onGhostBlock = false;

                AABB playerBox = AABB.PLAYER_COLLISION_BOX.clone().shift(from.toVector().setY(to.getY()));

                for (BlockPosition blockPos : ghostBlocks.keySet()) {
                    AABB blockBox = new AABB(
                            blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                            blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1
                    );

                    onGhostBlock |= Math.abs(playerBox.getMinY() - blockBox.getMaxY()) <= (to.isUpdatePos() ? 1E-6 : 0.03)
                            && playerBox.getMinX() < blockBox.getMaxX()
                            && playerBox.getMaxX() > blockBox.getMinX()
                            && playerBox.getMinZ() < blockBox.getMaxZ()
                            && playerBox.getMaxZ() > blockBox.getMinZ();
                }
            }

            wasInWater = inWater;
            inWater = false;

            AABB water = AABB.WATER_COLLISION_BOX.clone().shift(toVector);
            for (Block block : water.getBlocks(world)) {
                Material mat = block.getType();
                inWater |= mat == Material.WATER || mat == Material.STATIONARY_WATER;
            }

            wasInLava = inLava;
            inLava = false;

            AABB lava = AABB.LAVA_COLLISION_BOX.clone().shift(toVector);
            for (Block block : lava.getBlocks(world)) {
                Material mat = block.getType();
                inLava |= mat == Material.LAVA || mat == Material.STATIONARY_LAVA;
            }

            lastOnGroundRelly = onGroundReally;
            onGroundReally = AdjacentBlocks.onGroundReally(to.toLocation(world),
                    data.getMoveProcessor().getDeltaY(), true, 0.02);

            if (onGroundReally) data.getMoveProcessor().setLastGroundLoc(from.clone());

            step = testStep(from, to, world);
            jumped = testJumped(from, lastPosLoc, to, world, AABB.PLAYER_COLLISION_BOX.clone().shift(toVector));
            activeBlocks = testActiveBlocks(from, lastPosLoc, world);
            inWeb = testWeb();
            liquidsAndDirections = testWater(to, world);
            waterFlowForce = computeWaterFlowForce();
            onEdge = testOnEdge(to, world);
            climbing = testClimbable(from, world);
            boolean pushedByPiston = testPiston(from, lastPosLoc, world);

            ticksSinceNearWall = nearWall ? 0 : ++ticksSinceNearWall;
            ticksSinceOnSlime = onSlime ? 0 : ++ticksSinceOnSlime;
            ticksSinceUnderBlock = underBlock ? 0 : ++ticksSinceUnderBlock;
            ticksSinceInWater = inWater ? 0 : ++ticksSinceInWater;
            ticksSinceInLava = inLava ? 0 : ++ticksSinceInLava;
            ticksSinceClimb = climbing ? 0 : ++ticksSinceClimb;
            ticksSincePushedByPiston = pushedByPiston ? 0 : ++ticksSincePushedByPiston;

            waterTicks = inWater ? ++waterTicks : 0;
            lavaTicks = inLava ? ++lavaTicks : 0;
            climbTicks = climbing ? ++climbTicks : 0;

        } else if (packet instanceof CPacketBlockPlace) {
            CPacketBlockPlace wrapper = (CPacketBlockPlace) packet;

            if (!wrapper.isPlacedBlock()) return;
            BlockPosition key = wrapper.getBlockPosition().add(new BlockPosition(wrapper.getBlockFaceNormal()));

            ghostBlocks.put(key, false);

            data.getPingProcessor().confirm(() -> ghostBlocks.put(key, true));
        }
    }

    @Override
    public void postProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            ghostBlocks.entrySet().removeIf(Map.Entry::getValue);
        }
    }

    private boolean testStep(PlayerLocation from, PlayerLocation to, World world) {
        Vector prevPos = from.toVector();
        Vector extrapolate = from.toVector();

        double lastDeltaY = data.getMoveProcessor().getLastDeltaY();
        extrapolate.setY(extrapolate.getY() + (lastOnGroundRelly ? -0.0784 : (lastDeltaY - 0.08) * 0.98));

        AABB box = AABB.PLAYER_COLLISION_BOX.clone().shift(extrapolate);

        List<AABB> verticalCollision = box.getBlockAABBs(world, Material.WEB);
        if (verticalCollision.isEmpty() && !from.isOnGround()) return false;

        double highestVertical = extrapolate.getY();
        for (AABB blockAABB : verticalCollision) {
            double aabbMaxY = blockAABB.getMaxY();
            if (aabbMaxY > highestVertical) {
                highestVertical = aabbMaxY;
            }
        }

        box = AABB.PLAYER_COLLISION_BOX.clone().shift(to.toVector().setY(highestVertical)).expand(0, -1E-11, 0);

        List<AABB> horizontalCollision = box.getBlockAABBs(world, Material.WEB);
        if (horizontalCollision.isEmpty()) return false;

        double expectedY = prevPos.getY();
        double highestPointOnAABB = -1;
        for (AABB blockAABB : horizontalCollision) {
            double blockAABBY = blockAABB.getMaxY();
            if (blockAABBY - prevPos.getY() > 0.6) return false;

            if (blockAABBY > expectedY) expectedY = blockAABBY;
            if (blockAABBY > highestPointOnAABB) highestPointOnAABB = blockAABBY;
        }

        return (to.isOnGround() || onGroundReally)
                && Math.abs(prevPos.getY() - highestPointOnAABB) > 1E-4
                && Math.abs(to.getY() - expectedY) < 1E-4;
    }

    private boolean testJumped(PlayerLocation from, PlayerLocation lastPosLoc, PlayerLocation to, World world, AABB aabb) {
        Vector actualFrom = from.isUpdatePos() ? from.toVector() : lastPosLoc.toVector();

        float deltaY = (float) (to.getY() - actualFrom.getY());
        float expectedDY = Math.max(data.getAttributeProcessor().getAttributeJump(), 0F);

        boolean leftGround = from.isOnGround() && !to.isOnGround();

        AABB box = AABB.PLAYER_COLLISION_BOX.clone()
                .expand(-1E-6, -1E-6, -1E-6)
                .shift(to.toVector().add(new Vector(0, expectedDY, 0)));

        boolean collidedNow = !box.getBlockAABBs(world).isEmpty();

        box = AABB.PLAYER_COLLISION_BOX.clone()
                .expand(-1E-6, -1E-6, -1E-6)
                .shift(from.toVector().add(new Vector(0, expectedDY, 0)));

        boolean collidedBefore = !box.getBlockAABBs(world).isEmpty();

        if (collidedNow && !collidedBefore && leftGround && deltaY == 0) expectedDY = 0;

        EnumSet<Material> touchedBlocks = aabb.getMaterials(world);
        if (touchedBlocks.contains(Material.WEB)) {
            if (to.isUpdatePos()) {
                expectedDY *= 0.05F;
            } else {
                expectedDY = 0;
            }
        }

        AABB feet = new AABB(
                from.getX() - 0.23, from.getY() - 0.3, from.getZ() - 0.23,
                from.getX() + 0.23, from.getY(), from.getZ() + 0.23
        );

        boolean onAirApprox = feet.getBlockAABBs(world).isEmpty();

        AABB playerBox = AABB.PLAYER_COLLISION_BOX.clone().shift(from.toVector());
        playerBox.setMinY(playerBox.getMinY() + 0.001);

        boolean touchingBlock = !playerBox.getBlockAABBs(world).isEmpty();
        boolean blockGlitch = onAirApprox && touchingBlock;

        return ((expectedDY == 0 && from.isOnGround()) || leftGround)
                && (deltaY == expectedDY || underBlock) && !blockGlitch;
    }

    private List<Block> testActiveBlocks(PlayerLocation from, PlayerLocation lastPosLoc, World world) {
        Vector actualFrom = from.isUpdatePos() ? from.toVector() : lastPosLoc.toVector();

        AABB test = AABB.PLAYER_COLLISION_BOX.clone().expand(-0.001, -0.001, -0.001).shift(actualFrom);

        return test.getBlocks(world);
    }

    private boolean testPiston(PlayerLocation from, PlayerLocation lastPosLoc, World world) {
        Vector actualFrom = from.isUpdatePos() ? from.toVector() : lastPosLoc.toVector();

        AABB test = AABB.PLAYER_COLLISION_BOX.clone().expand(0.001, 0.001, 0.001).shift(actualFrom);

        for (Block block : test.getBlocks(world)) {
            if (MaterialUtil.PISTONS.contains(block.getType())) {
                return true;
            }
        }

        return false;
    }

    private boolean testWeb() {
        for (Block block : activeBlocks) {
            if (block.getType() == Material.WEB) {
                return true;
            }
        }
        return false;
    }

    private List<Pair<Block, Vector>> testWater(PlayerLocation to, World world) {
        AABB liquidTest = AABB.WATER_COLLISION_BOX.clone().shift(to.toVector());

        List<Pair<Block, Vector>> liquids = new ArrayList<>();
        List<Block> blocks = liquidTest.getBlocks(world);

        for (Block block : blocks) {
            if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) {
                Vector direction = VISITOR.getLiquidFlowDirection(block);
                liquids.add(new Pair<>(block, direction));
            }
        }

        return liquids;
    }

    private Vector computeWaterFlowForce() {
        Vector finalForce = new Vector();

        for (Pair<Block, Vector> liquid : liquidsAndDirections) {
            Material mat = liquid.getKey().getType();

            if (mat == Material.STATIONARY_WATER || mat == Material.WATER) {
                finalForce.add(liquid.getValue());
            }
        }

        if (finalForce.lengthSquared() > 0.0 && !data.getAttributeProcessor().isFlying()) {
            finalForce.normalize();
            finalForce.multiply(0.014F);
        }

        return finalForce;
    }

    public boolean testOnEdge(PlayerLocation to, World world) {
        Block block = BlockUtil.getBlockAsync(to.toLocation(world).subtract(0.0, 2.0, 0.0));

        return block != null && block.getType() == Material.AIR;
    }

    public boolean testClimbable(PlayerLocation from, World world) {
        Block block = BlockUtil.getBlockAsync(from.toLocation(world));
        if (block == null) return false;

        return block.getType() == Material.VINE || block.getType() == Material.LADDER;
    }

    public Set<BlockPosition> getGhostBlocks() {
        return ghostBlocks.keySet();
    }

    public List<Block> getActiveBlocks() {
        return activeBlocks;
    }

    public boolean isUnderBlock() {
        return underBlock;
    }

    public boolean wasInWater() {
        return wasInWater;
    }

    public boolean isInWater() {
        return inWater;
    }

    public boolean wasInLava() {
        return wasInLava;
    }

    public boolean isInLava() {
        return inLava;
    }

    public boolean isOnSlime() {
        return onSlime;
    }

    public boolean isNearWall() {
        return nearWall;
    }

    public boolean isCollideX() {
        return collideX;
    }

    public boolean isCollideZ() {
        return collideZ;
    }

    public boolean isInWeb() {
        return inWeb;
    }

    public boolean isLastOnGroundRelly() {
        return lastOnGroundRelly;
    }

    public boolean isOnGroundReally() {
        return onGroundReally;
    }

    public boolean isStep() {
        return step;
    }

    public boolean isJump() {
        return jumped;
    }

    public boolean isOnEdge() {
        return onEdge;
    }

    public boolean isClimbing() {
        return climbing;
    }

    public boolean isOnGhostBlock() {
        return onGhostBlock;
    }

    public Vector getWaterFlowForce() {
        return waterFlowForce;
    }

    public int getTicksSinceNearWall() {
        return ticksSinceNearWall;
    }

    public int getTicksSinceOnSlime() {
        return ticksSinceOnSlime;
    }

    public int getTicksSinceUnderBlock() {
        return ticksSinceUnderBlock;
    }

    public int getTicksSinceInWater() {
        return ticksSinceInWater;
    }

    public int getTicksSinceInLava() {
        return ticksSinceInLava;
    }

    public int getTicksSinceClimb() {
        return ticksSinceClimb;
    }

    public int getTicksSincePushedByPiston() {
        return ticksSincePushedByPiston;
    }

    public int getWaterTicks() {
        return waterTicks;
    }

    public int getLavaTicks() {
        return lavaTicks;
    }

    public int getClimbTicks() {
        return climbTicks;
    }
}
