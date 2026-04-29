package me.dw1e.axiom.packet.wrapper.server;

import com.comphenix.protocol.events.PacketContainer;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class SPacketCloseWindow extends WrappedPacket {

    private final int windowId;

    public SPacketCloseWindow(PacketContainer container) {
        windowId = container.getIntegers().read(0);
    }

    public int getWindowId() {
        return windowId;
    }
}
