package me.dw1e.axiom.packet.wrapper.server;

import com.comphenix.protocol.events.PacketContainer;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class SPacketHeldItemSlot extends WrappedPacket {

    private final int slot;

    public SPacketHeldItemSlot(PacketContainer container) {
        slot = container.getIntegers().read(0);
    }

    public int getSlot() {
        return slot;
    }
}
