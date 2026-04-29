package me.dw1e.axiom.packet.wrapper.client;

import com.comphenix.protocol.events.PacketContainer;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class CPacketCustomPayload extends WrappedPacket {

    private final String payload;

    public CPacketCustomPayload(PacketContainer container) {
        payload = container.getStrings().read(0);
    }

    public String getPayload() {
        return payload;
    }
}
