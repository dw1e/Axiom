package me.dw1e.axiom.data.processor.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.check.impl.badpacket.BadPacketP;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.data.processor.Processor;
import me.dw1e.axiom.misc.Pair;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.misc.util.BlockUtil;
import me.dw1e.axiom.misc.util.MathUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.server.SPacketEntityVelocity;
import me.dw1e.axiom.packet.wrapper.server.SPacketPosition;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

public final class MoveProcessor extends Processor {

    private final Deque<Pair<PlayerLocation, Integer>> pendingTeleports = new ArrayDeque<>();

    private final PlayerLocation to;
    private PlayerLocation previous, from;
    private PlayerLocation lastPosLoc;
    private PlayerLocation lastGroundLoc;

    private boolean lastMathGround, mathGround;

    private double lastDistance;
    private int lastOffsetTick;

    private int maxVelocityTicks;

    private int ticksSinceTeleport = 100;
    private int ticksSinceVelocity = 100;

    private int groundTicks, airTicks;

    private float lastLastFriction, lastFriction, friction;

    private float accelYaw, accelPitch;

    private float lastLastDeltaYaw, lastLastDeltaPitch;
    private float lastDeltaYaw, lastDeltaPitch;
    private float deltaYaw, deltaPitch;

    private double lastPosDeltaY;

    private double lastLastDeltaY;
    private double lastDeltaX, lastDeltaY, lastDeltaZ, lastDeltaXZ;
    private double deltaX, deltaY, deltaZ, deltaXZ;

    private double velocityX, velocityY, velocityZ, velocityXZ;

    public MoveProcessor(PlayerData data) {
        super(data);

        Location loc = data.getPlayer().getLocation().clone();
        lastGroundLoc = lastPosLoc = previous = from = to = new PlayerLocation(
                loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(),
                loc.getY() % 0.015625 == 0.0, false, false);
    }

    public void preProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            CPacketFlying wrapper = (CPacketFlying) packet;

            previous = from.clone();
            from = to.clone();
            if (from.isUpdatePos()) lastPosLoc = from.clone();

            to.setUpdatePos(wrapper.isPosition());
            if (wrapper.isPosition()) {
                to.setX(wrapper.getX());
                to.setY(wrapper.getY());
                to.setZ(wrapper.getZ());
            }

            to.setUpdateRot(wrapper.isRotation());
            if (wrapper.isRotation()) {
                to.setYaw(wrapper.getYaw());
                to.setPitch(wrapper.getPitch());
            }

            if (wrapper.isOnGround()) {
                to.setOnGround(true);

                ++groundTicks;
                airTicks = 0;
            } else {
                to.setOnGround(false);

                groundTicks = 0;
                ++airTicks;
            }

            ++ticksSinceTeleport;

            Pair<PlayerLocation, Integer> tpPair = pendingTeleports.peek();
            if (tpPair != null) {

                PlayerLocation tpLoc = tpPair.getKey();

                if (wrapper.isPosition() && wrapper.isRotation() && to.matches(tpLoc)) {
                    pendingTeleports.poll();

                    from = tpLoc.clone();
                    ticksSinceTeleport = 0;

                } else {
                    int elapsedTicks = data.getTick() - tpPair.getValue();
                    int pingTicks = data.getPingProcessor().getPingTicks();

                    if (elapsedTicks > pingTicks + 40) {
                        data.getCheck(BadPacketP.class).flag("size=" + pendingTeleports.size());

                        pendingTeleports.clear();
                    }
                }
            }

            double distance = to.distanceSquared(from);

            if (lastDistance > 0.0 && distance == 0.0 && !wrapper.isPosition()) lastOffsetTick = data.getTick();

            lastDistance = distance;

            lastMathGround = mathGround;
            mathGround = wrapper.getY() % 0.015625 == 0.0;

            lastLastDeltaYaw = lastDeltaYaw;
            lastLastDeltaPitch = lastDeltaPitch;

            lastLastDeltaY = lastDeltaY;

            lastDeltaYaw = deltaYaw;
            lastDeltaPitch = deltaPitch;

            deltaYaw = Math.abs(MathUtil.wrapAngleTo180(to.getYaw() - from.getYaw()));
            deltaPitch = Math.abs(to.getPitch() - from.getPitch());

            accelYaw = Math.abs(deltaYaw - lastDeltaYaw);
            accelPitch = Math.abs(deltaPitch - lastDeltaPitch);

            lastDeltaX = deltaX;
            lastDeltaY = deltaY;
            lastDeltaZ = deltaZ;
            lastDeltaXZ = deltaXZ;

            if (from.isUpdatePos()) lastPosDeltaY = lastDeltaY;

            deltaX = to.getX() - from.getX();
            deltaY = to.getY() - from.getY();
            deltaZ = to.getZ() - from.getZ();
            deltaXZ = MathUtil.hypot(deltaX, deltaZ);

            lastLastFriction = lastFriction;
            lastFriction = friction;
            friction = computeFriction();

        } else if (packet instanceof SPacketPosition) {
            SPacketPosition wrapper = (SPacketPosition) packet;

            PlayerLocation tpLoc = new PlayerLocation(
                    wrapper.getX(), wrapper.getY(), wrapper.getZ(),
                    wrapper.getYaw(), wrapper.getPitch()
            );

            pendingTeleports.add(new Pair<>(tpLoc, data.getTick()));

        } else if (packet instanceof SPacketEntityVelocity) {
            SPacketEntityVelocity wrapper = (SPacketEntityVelocity) packet;

            if (wrapper.getEntityId() == data.getPlayer().getEntityId()) {
                data.getPingProcessor().confirm(() -> {
                    velocityX = wrapper.getX();
                    velocityY = wrapper.getY();
                    velocityZ = wrapper.getZ();

                    velocityXZ = MathUtil.hypot(wrapper.getX(), wrapper.getZ());

                    ticksSinceVelocity = 0;
                    maxVelocityTicks = (int) Math.max(Math.round(20.0 * Math.pow(wrapper.getY(), 0.6)), 8);
                });
            }
        }
    }

    public void postProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            ++ticksSinceVelocity;

            velocityX = velocityY = velocityZ = 0.0;
        }
    }

    private float computeFriction() {
        boolean flying = data.getAttributeProcessor().isFlying();

        if (data.getCollideProcessor().isInWater() && !flying) {
            float friction = 0.8F, depthStrider = 0.0F;

            ItemStack boots = data.getPlayer().getInventory().getBoots();

            if (boots != null) depthStrider = boots.getEnchantmentLevel(Enchantment.DEPTH_STRIDER);
            if (depthStrider > 3.0F) depthStrider = 3.0F;

            if (!to.isOnGround()) depthStrider *= 0.5F;
            if (depthStrider > 0.0F) friction += (0.546F - friction) * depthStrider / 3.0F;

            return friction;

        } else if (data.getCollideProcessor().isInLava() && !flying) {
            return 0.5F;

        } else {
            float friction = 0.91F;

            if (to.isOnGround()) {
                Block blockBelow = BlockUtil.getBlockAsync(new Location(data.getPlayer().getWorld(),
                        Math.floor(to.getX()), Math.floor(to.getY() - 1), Math.floor(to.getZ())));

                friction *= blockBelow == null ? 0.6F : BlockUtil.getSlipperiness(blockBelow.getType());
            }

            return friction;
        }
    }

    // 提示: 会误判 Spigot 的 xxx moved too quickly! 此方法仅适合短距离回弹
    public void sendTeleport(PlayerLocation location) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.POSITION);

        packet.getDoubles().write(0, location.getX());
        packet.getDoubles().write(1, location.getY());
        packet.getDoubles().write(2, location.getZ());
        packet.getFloat().write(0, location.getYaw());
        packet.getFloat().write(1, location.getPitch());
        packet.getModifier().write(5, Collections.emptySet());

        Axiom.getPlugin().getProtocolManager().sendServerPacket(data.getPlayer(), packet);
    }

    public PlayerLocation getPrevious() {
        return previous;
    }

    public PlayerLocation getFrom() {
        return from;
    }

    public PlayerLocation getTo() {
        return to;
    }

    public PlayerLocation getLastPosLoc() {
        return lastPosLoc;
    }

    public PlayerLocation getLastGroundLoc() {
        return lastGroundLoc;
    }

    public void setLastGroundLoc(PlayerLocation location) {
        lastGroundLoc = location;
    }

    public boolean isLastMathGround() {
        return lastMathGround;
    }

    public boolean isMathGround() {
        return mathGround;
    }

    public int getGroundTicks() {
        return groundTicks;
    }

    public int getAirTicks() {
        return airTicks;
    }

    public boolean isOffsetMotion() {
        return isOffsetMotion(4);
    }

    public boolean isOffsetMotion(int ticks) {
        return data.getTick() - lastOffsetTick < ticks;
    }

    public boolean isOffsetYMotion() {
        return from.isOnGround() && !to.isOnGround() && deltaY == 0.0 && !from.isUpdatePos();
    }

    public boolean isTeleporting() {
        return !pendingTeleports.isEmpty();
    }

    public int getMaxVelocityTicks() {
        return maxVelocityTicks;
    }

    public int getTicksSinceTeleport() {
        return ticksSinceTeleport;
    }

    public int getTicksSinceVelocity() {
        return ticksSinceVelocity;
    }

    public float getLastLastDeltaYaw() {
        return lastLastDeltaYaw;
    }

    public float getLastDeltaYaw() {
        return lastDeltaYaw;
    }

    public float getDeltaYaw() {
        return deltaYaw;
    }

    public float getLastLastDeltaPitch() {
        return lastLastDeltaPitch;
    }

    public float getLastDeltaPitch() {
        return lastDeltaPitch;
    }

    public float getDeltaPitch() {
        return deltaPitch;
    }

    public float getAccelYaw() {
        return accelYaw;
    }

    public float getAccelPitch() {
        return accelPitch;
    }

    public double getLastPosDeltaY() {
        return lastPosDeltaY;
    }

    public double getLastLastDeltaY() {
        return lastLastDeltaY;
    }

    public double getLastDeltaX() {
        return lastDeltaX;
    }

    public double getLastDeltaY() {
        return lastDeltaY;
    }

    public double getLastDeltaZ() {
        return lastDeltaZ;
    }

    public double getLastDeltaXZ() {
        return lastDeltaXZ;
    }

    public double getDeltaX() {
        return deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    public double getDeltaZ() {
        return deltaZ;
    }

    public double getDeltaXZ() {
        return deltaXZ;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public double getVelocityZ() {
        return velocityZ;
    }

    public double getVelocityXZ() {
        return velocityXZ;
    }

    public float getLastFriction() {
        return lastLastFriction;
    }

    public float getFriction() {
        return lastFriction;
    }
}
