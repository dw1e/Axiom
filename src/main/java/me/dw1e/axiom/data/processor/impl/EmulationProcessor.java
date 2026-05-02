package me.dw1e.axiom.data.processor.impl;

import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.data.processor.Processor;
import me.dw1e.axiom.misc.math.OptiFineMath;
import me.dw1e.axiom.misc.math.VanillaMath;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import org.bukkit.util.Vector;

public final class EmulationProcessor extends Processor {

    // 模拟处理器: 模拟玩家可能的移动方式, 看哪一种最接近真实情况.

    // 当前最优模拟与真实移动的误差
    private double minDistance = Double.MAX_VALUE;

    // 当前最优模拟结果下, 玩家的状态组合
    private boolean sprint, jump, using, hitSlowdown, fastMath;

    public EmulationProcessor(PlayerData data) {
        super(data);
    }

    @Override
    public void preProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            Vector realMotion = new Vector(
                    data.getMoveProcessor().getDeltaX(),
                    0.0,
                    data.getMoveProcessor().getDeltaZ()
            );

            minDistance = Double.MAX_VALUE;

            boolean ground = data.getMoveProcessor().getFrom().isOnGround();
            boolean sneaking = data.getActionProcessor().isSneaking();

            iteration:
            {
                for (int f = -1; f < 2; f++) {
                    for (int s = -1; s < 2; s++) {
                        for (int sp = 0; sp < 2; sp++) {
                            for (int jp = 0; jp < 2; jp++) {
                                for (int ui = 0; ui < 2; ui++) {
                                    for (int hs = 0; hs < 2; hs++) {
                                        for (int fm = 0; fm < 2; fm++) {
                                            boolean sprint = sp == 0;
                                            boolean jump = jp == 0;
                                            boolean using = ui == 0;
                                            boolean hitSlowdown = hs == 0;
                                            boolean fastMath = fm == 1;

                                            if (f <= 0.0F && sprint && ground) continue;

                                            float forward = f;
                                            float strafe = s;

                                            if (using) {
                                                forward *= 0.2F;
                                                strafe *= 0.2F;
                                            }

                                            if (sneaking) {
                                                forward *= 0.3F;
                                                strafe *= 0.3F;
                                            }

                                            forward *= 0.98F;
                                            strafe *= 0.98F;

                                            Vector motion = new Vector(
                                                    data.getMoveProcessor().getLastDeltaX(),
                                                    0.0,
                                                    data.getMoveProcessor().getLastDeltaZ()
                                            );

                                            float lastFriction = data.getMoveProcessor().getLastFriction();

                                            motion.multiply(new Vector(lastFriction, 0.0, lastFriction));

                                            if (data.getMoveProcessor().getTicksSinceVelocity() == 0) {
                                                motion = new Vector(
                                                        data.getMoveProcessor().getVelocityX(),
                                                        0.0,
                                                        data.getMoveProcessor().getVelocityZ()
                                                );
                                            }

                                            if (hitSlowdown) motion.multiply(new Vector(0.6F, 0.0, 0.6F));

                                            if (Math.abs(motion.getX()) < 0.005) motion.setX(0.0);
                                            if (Math.abs(motion.getZ()) < 0.005) motion.setZ(0.0);

                                            if (jump && sprint) {
                                                float radians = data.getMoveProcessor().getTo().getYaw() * 0.017453292F;

                                                motion.add(new Vector(
                                                        -sin(fastMath, radians) * 0.2F,
                                                        0.0,
                                                        cos(fastMath, radians) * 0.2F)
                                                );
                                            }

                                            float moveFlyingFriction;

                                            if (ground) {
                                                float moveSpeedMultiplier = 0.16277136F
                                                        / (float) Math.pow(data.getMoveProcessor().getFriction(), 3.0F);

                                                float attributeSpeed = data.getAttributeProcessor().getAttributeSpeed();

                                                moveFlyingFriction = attributeSpeed * moveSpeedMultiplier;
                                            } else {
                                                moveFlyingFriction = sprint ? 0.026F : 0.02F;
                                            }

                                            float[] moveFlying = moveFlying(forward, strafe, moveFlyingFriction, fastMath);

                                            motion.add(new Vector(moveFlying[0], 0.0, moveFlying[1]));

                                            if (data.getCollideProcessor().isInWeb()) {
                                                motion.multiply(new Vector(0.25, 0, 0.25));
                                            }

                                            if (data.getCollideProcessor().isCollideX()) {
                                                motion.setX(data.getMoveProcessor().getDeltaX());
                                            }
                                            if (data.getCollideProcessor().isCollideZ()) {
                                                motion.setZ(data.getMoveProcessor().getDeltaZ());
                                            }

                                            double distance = realMotion.distanceSquared(motion);

                                            if (distance < minDistance) {
                                                minDistance = distance;

                                                this.sprint = sprint;
                                                this.jump = jump;
                                                this.using = using;
                                                this.hitSlowdown = hitSlowdown;
                                                this.fastMath = fastMath;

                                                if (distance < 1E-14) break iteration;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    @Override
    public void postProcess(WrappedPacket packet) {

    }

    private float[] moveFlying(float moveForward, float moveStrafe, float friction, boolean fastMath) {
        float diagonal = moveStrafe * moveStrafe + moveForward * moveForward;

        float moveFlyingFactorX = 0.0F;
        float moveFlyingFactorZ = 0.0F;

        if (diagonal >= 1.0E-4F) {
            diagonal = VanillaMath.sqrt(diagonal);

            if (diagonal < 1.0F) diagonal = 1.0F;

            diagonal = friction / diagonal;

            float strafe = moveStrafe * diagonal;
            float forward = moveForward * diagonal;

            float rotationYaw = data.getMoveProcessor().getTo().getYaw();

            float f1 = sin(fastMath, rotationYaw * (float) Math.PI / 180.0F);
            float f2 = cos(fastMath, rotationYaw * (float) Math.PI / 180.0F);

            float factorX = strafe * f2 - forward * f1;
            float factorZ = forward * f2 + strafe * f1;

            moveFlyingFactorX = factorX;
            moveFlyingFactorZ = factorZ;
        }

        return new float[]{moveFlyingFactorX, moveFlyingFactorZ};
    }

    private float sin(boolean fastMath, float yaw) {
        return fastMath ? OptiFineMath.sin(yaw) : VanillaMath.sin(yaw);
    }

    private float cos(boolean fastMath, float yaw) {
        return fastMath ? OptiFineMath.cos(yaw) : VanillaMath.cos(yaw);
    }

    public boolean isSprint() {
        return sprint;
    }

    public boolean isJump() {
        return jump;
    }

    public boolean isUsing() {
        return using;
    }

    public boolean isHitSlowdown() {
        return hitSlowdown;
    }

    public boolean isFastMath() {
        return fastMath;
    }

    public double getMinDistance() {
        return minDistance;
    }
}
