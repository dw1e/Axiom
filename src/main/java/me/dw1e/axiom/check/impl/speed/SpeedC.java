package me.dw1e.axiom.check.impl.speed;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.misc.math.VanillaMath;
import me.dw1e.axiom.misc.util.MathUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

// 借鉴自 Hawk 作者 Islandscout
public final class SpeedC extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.SPEED, "C", "检查疾跑朝向")
                    .setVLToAlert(5)
                    .setThresholds(30);

    private int lastSprintTick;
    private boolean lastCollisionHorizontal;

    public SpeedC(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            PlayerLocation from = moveProcessor.getFrom();
            PlayerLocation to = moveProcessor.getTo();

            boolean collisionHorizontal = collideProcessor.isNearWall();
            Vector moveHorizontal = to.toVector().subtract(from.toVector()).setY(0.0);

            if (!actionProcessor.isSprinting()) lastSprintTick = data.getTick();

            if (collideProcessor.isInWater()
                    || collideProcessor.isClimbing()
                    || collideProcessor.getTicksSincePushedByPiston() < 2
                    || moveProcessor.getTicksSinceTeleport() < 4
                    || moveProcessor.getTicksSinceVelocity() == 0
                    || (collisionHorizontal && !lastCollisionHorizontal)
                    || actionProcessor.getTicksSinceInsideVehicle() < 4
                    || (data.getTick() - lastSprintTick < 2)
                    || moveHorizontal.lengthSquared() < 0.04
            ) return;

            double lastDeltaX = moveProcessor.getLastDeltaX();
            double lastDeltaZ = moveProcessor.getLastDeltaZ();

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

            if (actionProcessor.getTicksSinceHitSlowdown() == 0) {
                lastDeltaX *= 0.6F;
                lastDeltaZ *= 0.6F;
            }

            double deltaX = moveProcessor.getDeltaX();
            double deltaZ = moveProcessor.getDeltaZ();

            float friction = moveProcessor.getLastFriction();

            deltaX /= friction;
            deltaZ /= friction;

            float yaw = to.getYaw();

            if (collideProcessor.isJump()) {
                float yawRadians = yaw * 0.017453292F;

                deltaX += VanillaMath.sin(yawRadians) * 0.2F;
                deltaZ -= VanillaMath.cos(yawRadians) * 0.2F;
            }

            deltaX -= lastDeltaX;
            deltaZ -= lastDeltaZ;

            Vector moveForce = new Vector(deltaX, 0.0, deltaZ);
            Vector yawVec = MathUtil.getDirection(yaw, 0.0F);

            double angle = MathUtil.angle(yawVec, moveForce);

            if (angle > Math.PI / 4 + 0.3) {
                flag(String.format("a=%.5f", angle));
            } else {
                violations *= 0.999;
            }

            lastCollisionHorizontal = collisionHorizontal;
        }
    }

}
