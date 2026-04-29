package me.dw1e.axiom.packet.wrapper.server;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

import java.util.List;

public final class SPacketEntityMetadata extends WrappedPacket {

    private final int entityId;
    private final List<WrappedWatchableObject> watchableObjects;

    public SPacketEntityMetadata(PacketContainer container) {
        entityId = container.getIntegers().read(0);
        watchableObjects = container.getWatchableCollectionModifier().read(0);
    }

    public int getEntityId() {
        return entityId;
    }

    public List<WrappedWatchableObject> getWatchableObjects() {
        return watchableObjects;
    }

    public WrappedWatchableObject getObject(int index) {
        for (WrappedWatchableObject object : watchableObjects) {
            if (object.getIndex() == index) {
                return object;
            }
        }
        return null;
    }

    public boolean hasObject(int index) {
        return getObject(index) != null;
    }
}