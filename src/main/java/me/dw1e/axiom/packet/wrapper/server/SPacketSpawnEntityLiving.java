package me.dw1e.axiom.packet.wrapper.server;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class SPacketSpawnEntityLiving extends WrappedPacket {

    private final int entityId, type, x, y, z, data; // 某些实体会有附加数据, 例如投掷物方向
    private final byte yaw, pitch;

    public SPacketSpawnEntityLiving(PacketContainer container) {
        StructureModifier<Integer> integers = container.getIntegers();

        entityId = integers.read(0);
        type = integers.read(1);
        x = integers.read(2);
        y = integers.read(3);
        z = integers.read(4);
        data = integers.read(5);

        StructureModifier<Byte> bytes = container.getBytes();

        yaw = bytes.read(0);
        pitch = bytes.read(1);
    }

    public int getEntityId() {
        return entityId;
    }

    public int getType() {
        return type;
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

    public int getData() {
        return data;
    }
}