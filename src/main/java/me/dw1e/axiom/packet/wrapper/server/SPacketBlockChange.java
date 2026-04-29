package me.dw1e.axiom.packet.wrapper.server;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class SPacketBlockChange extends WrappedPacket {

    private final BlockPosition blockPosition;
    private final WrappedBlockData blockData;

    public SPacketBlockChange(PacketContainer container) {
        blockPosition = container.getBlockPositionModifier().read(0);
        blockData = container.getBlockData().read(0);
    }

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    public WrappedBlockData getBlockData() {
        return blockData;
    }
}
