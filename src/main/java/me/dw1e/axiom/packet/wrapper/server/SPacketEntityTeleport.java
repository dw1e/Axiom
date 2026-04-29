package me.dw1e.axiom.packet.wrapper.server;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class SPacketEntityTeleport extends WrappedPacket {

    private final int entityId, x, y, z;
    private final byte yaw, pitch;
    private final boolean onGround;

    public SPacketEntityTeleport(PacketContainer container) {
        StructureModifier<Integer> integers = container.getIntegers();

        entityId = integers.read(0);

        x = integers.read(1);
        y = integers.read(2);
        z = integers.read(3);

        StructureModifier<Byte> bytes = container.getBytes();

        yaw = bytes.read(0);
        pitch = bytes.read(1);

        onGround = container.getBooleans().read(0);
    }

    public int getEntityId() {
        return entityId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public byte getYaw() {
        return yaw;
    }

    public byte getPitch() {
        return pitch;
    }

    public boolean isOnGround() {
        return onGround;
    }
}
