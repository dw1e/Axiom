package me.dw1e.axiom.packet.wrapper.client;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class CPacketSteerVehicle extends WrappedPacket {

    private final float sideValue, forwardValue;
    private final boolean jump, dismount;

    public CPacketSteerVehicle(PacketContainer container) {
        StructureModifier<Float> floats = container.getFloat();

        sideValue = floats.read(0);
        forwardValue = floats.read(1);

        StructureModifier<Boolean> booleans = container.getBooleans();

        jump = booleans.read(0);
        dismount = booleans.read(1);
    }

    public float getSideValue() {
        return sideValue;
    }

    public float getForwardValue() {
        return forwardValue;
    }

    public boolean isJump() {
        return jump;
    }

    public boolean isDismount() {
        return dismount;
    }
}
