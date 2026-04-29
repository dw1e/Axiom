package me.dw1e.axiom.packet.wrapper.server;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class SPacketRemoveEntityEffect extends WrappedPacket {

    private final int entityId;
    private final int effectId;

    public SPacketRemoveEntityEffect(PacketContainer container) {
        StructureModifier<Integer> integers = container.getIntegers();

        entityId = integers.read(0);
        effectId = integers.read(1);
    }

    public int getEntityId() {
        return entityId;
    }

    public int getEffectId() {
        return effectId;
    }
}
