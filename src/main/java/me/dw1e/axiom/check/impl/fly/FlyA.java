package me.dw1e.axiom.check.impl.fly;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.AABB;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.List;

public final class FlyA extends Check {

    // TODO: 跳进水中时误判

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.FLY, "A", "检查垂直方向运动是否遵循游戏重力")
                    .setVLToAlert(5)
                    .setThresholds(30);

    private int streaks;

    public FlyA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            PlayerLocation from = moveProcessor.getFrom();
            PlayerLocation to = moveProcessor.getTo();

            double lastDeltaY = moveProcessor.getLastDeltaY();
            double deltaY = moveProcessor.getDeltaY();

            double predicted;
            double threshold = moveProcessor.isOffsetMotion(20) ? 0.03 : 1E-5;

            float attributeJump = attributeProcessor.getAttributeJump();

            if (collideProcessor.wasInWater()) {
                predicted = (lastDeltaY * 0.8) - 0.02;
                threshold += 0.1;

                if (collideProcessor.getTicksSinceInWater() == 1 && collideProcessor.isNearWall()) threshold = 0.48;

            } else if (collideProcessor.wasInLava()) {
                predicted = (lastDeltaY * 0.5) - 0.02;
                threshold += 0.1;

                if (collideProcessor.getTicksSinceInLava() == 1 && collideProcessor.isNearWall()) threshold = 0.48;

            } else {
                if (from.isOnGround()) {
                    if (collideProcessor.getTicksSinceOnSlime() == 1) {
                        predicted = (-moveProcessor.getLastLastDeltaY() - 0.08) * 0.98F;
                        threshold += 0.1;
                    } else {
                        predicted = deltaY > 0.0 ? attributeJump : -0.0784;
                    }
                } else {
                    if (collideProcessor.isInWater() || collideProcessor.isInLava()) {
                        double flowForce = computeWaterFlowForce();

                        predicted = ((lastDeltaY + flowForce - 0.08) * 0.98F) + flowForce;
                        threshold += 0.04;

                    } else {
                        predicted = (lastDeltaY - 0.08) * 0.98F;
                    }
                }
            }

            if (moveProcessor.getTicksSinceVelocity() == 0) {
                predicted = moveProcessor.getVelocityY();
            }

            if (collideProcessor.isInWeb()) {
                predicted *= 0.05;
                threshold += 0.05;
            }

            if (Math.abs(predicted) < 0.005) predicted = 0.0;

            if (moveProcessor.getTicksSinceTeleport() < 2) {
                predicted = 0.0;

                // 传送时地面状态永远为否, 但此时玩家可能起跳
                if (moveProcessor.isLastMathGround() && !moveProcessor.isMathGround()) {
                    threshold = attributeJump;
                }
            }

            if (collideProcessor.isUnderBlock()
                    && (moveProcessor.isOffsetYMotion() || moveProcessor.isOffsetMotion())) {
                predicted = 0.0;
            }

            double offset = Math.abs(deltaY - predicted);

            if (collideProcessor.getTicksSinceInWater() == 2) threshold += 0.0475;

            if (collideProcessor.getTicksSinceOnSlime() < 38
                    && moveProcessor.isOffsetMotion()) threshold += 0.0784;

            boolean exempt = to.isOnGround()
                    || data.getTick() < 20
                    || attributeProcessor.isFlying()
                    || attributeProcessor.getTicksSinceAbilityChange() == 0
                    || collideProcessor.isStep()
                    || collideProcessor.isJump()
                    || collideProcessor.isClimbing()
                    || collideProcessor.isOnSlime()
                    || (collideProcessor.getTicksSinceUnderBlock() < 2 && deltaY != 0.0)
                    || collideProcessor.getTicksSincePushedByPiston() < 2
                    || actionProcessor.getTicksSinceInsideVehicle() < 4;

            if (offset > threshold && !exempt) {
                flag(String.format(
                        "o=%.5f (%.5f/%.5f), t=%.5f, s=%s",
                        offset, deltaY, predicted, threshold, ++streaks
                ), Math.max(Math.min(streaks, 5), (offset - threshold) * 5.0));

            } else {
                streaks = 0;
                violations *= 0.995;
            }
        }
    }

    private double computeWaterFlowForce() {
        Vector waterFlow = new Vector();

        PlayerLocation from = moveProcessor.getFrom();
        AABB liquidTest = AABB.WATER_COLLISION_BOX.clone().shift(from.toVector());

        List<Block> blocks = liquidTest.getBlocks(data.getPlayer().getWorld());
        for (Block block : blocks) {
            if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) {
                Vector direction = VISITOR.getLiquidFlowDirection(block);
                waterFlow.add(direction);
            }
        }

        Vector flowForce = new Vector();
        if (waterFlow.lengthSquared() > 0 && !attributeProcessor.isFlying()) {
            waterFlow.normalize();
            waterFlow.multiply(0.014F);
            flowForce.add(waterFlow);
        }

        return flowForce.getY();
    }
}
