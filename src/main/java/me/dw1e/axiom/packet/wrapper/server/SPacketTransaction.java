package me.dw1e.axiom.packet.wrapper.server;

import com.comphenix.protocol.events.PacketContainer;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class SPacketTransaction extends WrappedPacket {

    private final int windowId;
    private final short actionId;
    private final boolean accepted;

    public SPacketTransaction(PacketContainer container) {
        windowId = container.getIntegers().read(0);
        actionId = container.getShorts().read(0);
        accepted = container.getBooleans().read(0);
    }

    public int getWindowId() {
        return windowId;
    }

    public short getActionId() {
        return actionId;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
