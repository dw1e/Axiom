package me.dw1e.axiom.packet.wrapper.client;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class CPacketEntityAction extends WrappedPacket {

    private final int entityId, auxId;
    private final EnumWrappers.PlayerAction action;

    public CPacketEntityAction(PacketContainer container) {
        StructureModifier<Integer> integers = container.getIntegers();

        entityId = integers.read(0);
        auxId = integers.read(1);

        action = container.getPlayerActions().read(0);
    }

    public int getEntityId() {
        return entityId;
    }

    public int getAuxId() {
        return auxId;
    }

    public EnumWrappers.PlayerAction getAction() {
        return action;
    }
}
