package me.dw1e.axiom.packet.wrapper.server;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class SPacketEntityEffect extends WrappedPacket {

    private final int entityId, duration;
    private final byte effectId, amplifier, showParticles;

    public SPacketEntityEffect(PacketContainer container) {
        StructureModifier<Integer> integers = container.getIntegers();

        entityId = integers.read(0);
        duration = integers.read(1);

        StructureModifier<Byte> bytes = container.getBytes();

        effectId = bytes.read(0);
        amplifier = bytes.read(1);
        showParticles = bytes.read(2);
    }

    public int getEntityId() {
        return entityId;
    }

    public byte getEffectId() {
        return effectId;
    }

    public byte getAmplifier() {
        return amplifier;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isShowParticles() {
        return showParticles == 1;
    }
}
