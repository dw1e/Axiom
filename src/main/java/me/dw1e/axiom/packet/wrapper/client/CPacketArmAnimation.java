package me.dw1e.axiom.packet.wrapper.client;

import com.comphenix.protocol.events.PacketContainer;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class CPacketArmAnimation extends WrappedPacket {

    private final long timestamp;

    public CPacketArmAnimation(PacketContainer container) {
        timestamp = container.getLongs().read(0);
    }

    public long getTimestamp() {
        return timestamp;
    }
}
