package me.dw1e.axiom.packet.wrapper.client;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class CPacketBlockDig extends WrappedPacket {

    private final BlockPosition blockPosition;
    private final EnumWrappers.Direction direction;
    private final EnumWrappers.PlayerDigType playerDigType;

    public CPacketBlockDig(PacketContainer container) {
        blockPosition = container.getBlockPositionModifier().read(0);
        direction = container.getDirections().read(0);
        playerDigType = container.getPlayerDigTypes().read(0);
    }

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    public EnumWrappers.Direction getDirection() {
        return direction;
    }

    public EnumWrappers.PlayerDigType getPlayerDigType() {
        return playerDigType;
    }
}
