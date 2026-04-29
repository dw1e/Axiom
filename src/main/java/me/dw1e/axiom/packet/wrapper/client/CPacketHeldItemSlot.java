package me.dw1e.axiom.packet.wrapper.client;

import com.comphenix.protocol.events.PacketContainer;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class CPacketHeldItemSlot extends WrappedPacket {

    private final int slot;

    public CPacketHeldItemSlot(PacketContainer container) {
        slot = container.getIntegers().read(0);
    }

    public int getSlot() {
        return slot;
    }
}
