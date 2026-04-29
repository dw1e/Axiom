package me.dw1e.axiom.check.impl.speed;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.AABB;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.misc.util.BlockUtil;
import me.dw1e.axiom.misc.util.MathUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Set;

// 借鉴自 Hawk 作者 Islandscout
public final class SpeedB extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.SPEED, "B", "检查灵活动作")
                    .setVLToAlert(5)
                    .setThresholds(30);

    private static final double THRESHOLD = Math.toRadians(0.5);

    private Integer lastIdleTick;
    private boolean wasSneakingOnEdge;

    public SpeedB(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            PlayerLocation from = moveProcessor.getFrom();
            PlayerLocation to = moveProcessor.getTo();

            Block footBlock = BlockUtil.getBlockAsync(data.getPlayer().getLocation().clone().add(0, -0.2, 0));
            if (footBlock == null) return;

            long ticksSinceIdle = data.getTick() - (lastIdleTick == null ? lastIdleTick = data.getTick() : lastIdleTick);

            boolean sneakEdge = actionProcessor.isSneaking()
                    && (!BlockUtil.isReallySolid(footBlock) || footBlock.getType() == Material.LADDER)
                    && to.isOnGround();

            boolean onSlimeBlock = (moveProcessor.getPrevious().isOnGround() || from.isOnGround())
                    && footBlock.getType() == Material.SLIME_BLOCK;

            Set<Material> collidedMats = AABB.PLAYER_COLLISION_BOX.clone().shift(from.toVector()).getMaterials(data.getPlayer().getWorld());
            boolean nearLiquid = testLiquid(collidedMats);

            if (moveProcessor.getTicksSinceTeleport() < 5
                    || moveProcessor.getTicksSinceVelocity() < 2
                    || actionProcessor.getTicksSinceInsideVehicle() < 5
                    || collideProcessor.getTicksSinceNearWall() < 2
                    || collideProcessor.getTicksSincePushedByPiston() < 2
                    || collideProcessor.isJump()
                    || collideProcessor.isNearWall()
                    || (collideProcessor.isStep() && actionProcessor.isSprinting())
                    || !to.isUpdatePos()
                    || ticksSinceIdle <= 2
                    || sneakEdge
                    || onSlimeBlock
                    || nearLiquid
                    || collidedMats.contains(Material.LADDER)
                    || collidedMats.contains(Material.VINE)
                    || wasSneakingOnEdge) {
                prepareNextMove(to.isUpdatePos(), sneakEdge);
                return;
            }

            double lastDeltaX = moveProcessor.getLastDeltaX();
            double lastDeltaZ = moveProcessor.getLastDeltaZ();

            if (actionProcessor.getTicksSinceHitSlowdown() == 0) {
                lastDeltaX *= 0.6F;
                lastDeltaZ *= 0.6F;
            }

            for (Block block : collideProcessor.getActiveBlocks()) {
                if (block.getType() == Material.SOUL_SAND) {
                    lastDeltaX *= 0.4F;
                    lastDeltaZ *= 0.4F;
                }
                if (block.getType() == Material.WEB) {
                    lastDeltaX = 0.0;
                    lastDeltaZ = 0.0;
                    break;
                }
            }

            double friction = moveProcessor.getLastFriction();

            if (Math.abs(lastDeltaX * friction) < 0.005) lastDeltaX = 0.0;
            if (Math.abs(lastDeltaZ * friction) < 0.005) lastDeltaZ = 0.0;

            double dX = moveProcessor.getDeltaX();
            double dZ = moveProcessor.getDeltaZ();

            dX /= friction;
            dZ /= friction;

            dX -= lastDeltaX;
            dZ -= lastDeltaZ;

            Vector accelDir = new Vector(dX, 0.0, dZ);
            Vector yaw = MathUtil.getDirection(to.getYaw(), 0.0F);

            if (accelDir.lengthSquared() < 1E-6) {
                prepareNextMove(to.isUpdatePos(), false);
                return;
            }

            boolean vectorDir = accelDir.clone().crossProduct(yaw).dot(new Vector(0, 1, 0)) >= 0;
            double angle = (vectorDir ? 1 : -1) * MathUtil.angle(accelDir, yaw);

            double modulo = (angle % (Math.PI / 4)) * (4 / Math.PI);
            double error = Math.abs(modulo - Math.round(modulo)) * (Math.PI / 4);

            if (error > THRESHOLD) {
                flag(String.format("e=%.5f", error));
            } else {
                violations *= 0.99;
            }

            prepareNextMove(to.isUpdatePos(), false);
        }
    }

    private boolean testLiquid(Set<Material> mats) {
        for (Material mat : mats) {
            if (mat == Material.WATER || mat == Material.STATIONARY_WATER
                    || mat == Material.LAVA || mat == Material.STATIONARY_LAVA
            ) return true;
        }
        return false;
    }

    private void prepareNextMove(boolean updatePos, boolean sneakOnEdge) {
        if (!updatePos) lastIdleTick = data.getTick();

        wasSneakingOnEdge = sneakOnEdge;
    }

}
