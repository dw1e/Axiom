package me.dw1e.axiom.packet.wrapper.server;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class SPacketAbilities extends WrappedPacket {

    private final boolean invulnerable, flying, allowedFly, instantlyBuild;
    private final float flySpeed, walkSpeed;

    public SPacketAbilities(PacketContainer container) {
        StructureModifier<Boolean> booleans = container.getBooleans();

        invulnerable = booleans.read(0);
        flying = booleans.read(1);
        allowedFly = booleans.read(2);
        instantlyBuild = booleans.read(3);

        StructureModifier<Float> floats = container.getFloat();

        flySpeed = floats.read(0);
        walkSpeed = floats.read(1);
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public boolean isFlying() {
        return flying;
    }

    public boolean isAllowedFly() {
        return allowedFly;
    }

    public boolean isInstantlyBuild() {
        return instantlyBuild;
    }

    public float getFlySpeed() {
        return flySpeed;
    }

    public float getWalkSpeed() {
        return walkSpeed;
    }
}
