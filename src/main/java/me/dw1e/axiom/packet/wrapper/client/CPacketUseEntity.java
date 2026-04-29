package me.dw1e.axiom.packet.wrapper.client;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import org.bukkit.util.Vector;

public final class CPacketUseEntity extends WrappedPacket {

    private final int entityId;
    private final EnumWrappers.EntityUseAction action;
    private final Vector hitVec;

    public CPacketUseEntity(PacketContainer container) {
        entityId = container.getIntegers().read(0);
        action = container.getEntityUseActions().read(0);
        hitVec = container.getVectors().read(0);
    }

    public int getEntityId() {
        return entityId;
    }

    public EnumWrappers.EntityUseAction getAction() {
        return action;
    }

    public Vector getHitVec() {
        return hitVec;
    }
}
