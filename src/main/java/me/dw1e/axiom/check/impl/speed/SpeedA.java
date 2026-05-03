package me.dw1e.axiom.check.impl.speed;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import org.bukkit.util.Vector;

public final class SpeedA extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.SPEED, "A", "检查水平方向的移动速度")
                    .setVLToAlert(3);

    private Double lastDistance;

    public SpeedA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying && ((CPacketFlying) packet).isPosition()) {
            PlayerLocation from = moveProcessor.getFrom();
            PlayerLocation to = moveProcessor.getTo();

            float friction = moveProcessor.getFriction();
            float originalAttributeSpeed = attributeProcessor.getAttributeSpeed();

            double attribute = originalAttributeSpeed;

            boolean sprinting = actionProcessor.isSprinting();

            if (from.isOnGround()) {
                attribute *= 0.16277136F / Math.pow(friction, 3.0F);

                boolean jump = collideProcessor.isJump() || (collideProcessor.isStep() && actionProcessor.isSprinting());

                if (!to.isOnGround() && jump && sprinting) attribute += 0.2F;

            } else {
                attribute = sprinting ? 0.026F : 0.02F;
            }

            if (collideProcessor.isInWater()) {
                Vector move = new Vector(moveProcessor.getDeltaX(), 0.0, moveProcessor.getDeltaZ());
                Vector waterForce = collideProcessor.getWaterFlowForce().clone().setY(0.0);

                double waterForceLength = waterForce.length() + 0.003;
                double moveLength = move.length();
                double computedForce = moveLength == 0 ? waterForceLength : (move.dot(waterForce) / moveLength);

                attribute += computedForce;
            }

            if (collideProcessor.isInWeb()) attribute *= 0.25;

            if (moveProcessor.getTicksSinceVelocity() == 0) {
                double velocityXZ = moveProcessor.getVelocityXZ();

                if (velocityXZ > 0.0) attribute = velocityXZ;
            }

            double threshold = 1E-5;

            // 攻击时若强制疾跑一直切换状态会误判
            if (actionProcessor.getTicksSinceAttack() < 10) threshold += 0.03;

            // 从走路切换到疾跑的第一个tick, 实际移动仍由走路速度驱动
            if (sprinting && !actionProcessor.wasSprint()) {
                attribute = (originalAttributeSpeed / 1.3F) * 0.16277136F / (float) Math.pow(friction, 3.0F);
            }

            // 上上次未移动的误判
            if (!moveProcessor.getPrevious().isUpdatePos()) threshold += 0.01;

            // 例如: 卡铁砧一直被回弹
            if (moveProcessor.getTicksSinceTeleport() == 1) {
                lastDistance = (double) originalAttributeSpeed;
                threshold = originalAttributeSpeed;
            }

            double distance = moveProcessor.getDeltaXZ();

            if (lastDistance != null) {
                // 一些误判
                if (lastDistance < originalAttributeSpeed) threshold = originalAttributeSpeed;

                double excess = distance - lastDistance - attribute;

                boolean exempt = attributeProcessor.isFlying()
                        || collideProcessor.getTicksSincePushedByPiston() < 2
                        || actionProcessor.getTicksSinceInsideVehicle() < 4;

                if (excess > threshold && !exempt) {
                    flag(String.format("e=%.5f, t=%f", excess, threshold), Math.max(0.5, (excess - threshold) * 5.0));
                } else {
                    violations *= 0.995;
                }
            }

            lastDistance = distance * friction;
        }
    }
}
