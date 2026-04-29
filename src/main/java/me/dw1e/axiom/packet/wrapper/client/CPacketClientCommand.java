package me.dw1e.axiom.packet.wrapper.client;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class CPacketClientCommand extends WrappedPacket {

    private final EnumWrappers.ClientCommand clientCommand;

    public CPacketClientCommand(PacketContainer container) {
        clientCommand = container.getClientCommands().read(0);
    }

    public EnumWrappers.ClientCommand getClientCommand() {
        return clientCommand;
    }
}
