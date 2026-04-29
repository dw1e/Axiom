package me.dw1e.axiom.packet.wrapper.client;

import com.comphenix.protocol.events.PacketContainer;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class CPacketKeepAlive extends WrappedPacket {

    private final int id;

    public CPacketKeepAlive(PacketContainer container) {
        id = container.getIntegers().read(0);
    }

    public int getId() {
        return id;
    }
}
