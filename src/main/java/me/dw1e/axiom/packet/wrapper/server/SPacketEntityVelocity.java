package me.dw1e.axiom.packet.wrapper.server;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class SPacketEntityVelocity extends WrappedPacket {

    private final int entityId;
    private final double x, y, z;

    public SPacketEntityVelocity(PacketContainer container) {
        StructureModifier<Integer> integers = container.getIntegers();

        entityId = integers.read(0);
        x = integers.read(1) / 8000.0;
        y = integers.read(2) / 8000.0;
        z = integers.read(3) / 8000.0;
    }

    public int getEntityId() {
        return entityId;
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
}
