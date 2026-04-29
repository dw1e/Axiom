package me.dw1e.axiom.packet.wrapper.client;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public class CPacketFlying extends WrappedPacket {

    private final double x, y, z;
    private final float yaw, pitch;
    private final boolean onGround, position, rotation;

    public CPacketFlying(PacketContainer container) {
        StructureModifier<Double> doubles = container.getDoubles();

        x = doubles.read(0);
        y = doubles.read(1);
        z = doubles.read(2);

        StructureModifier<Float> floats = container.getFloat();

        yaw = floats.read(0);
        pitch = floats.read(1);

        StructureModifier<Boolean> booleans = container.getBooleans();

        onGround = booleans.read(0);
        position = booleans.read(1);
        rotation = booleans.read(2);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean isPosition() {
        return position;
    }

    public boolean isRotation() {
        return rotation;
    }
}
