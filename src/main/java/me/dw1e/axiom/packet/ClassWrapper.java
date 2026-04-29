package me.dw1e.axiom.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.*;
import me.dw1e.axiom.packet.wrapper.server.*;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("deprecation")
public final class ClassWrapper {

    // 在玩家卡顿时, 停止发送此集合内类型的数据包, 减少玩家使用 FakeLag/Blink 的优势
    public static final Set<PacketType> CACHE_PACKETS = new HashSet<>();

    // 用到的数据包
    private static final Map<PacketType, Constructor<? extends WrappedPacket>> PACKET_MAP = new HashMap<>();

    static {
        // ===== Client =====
        PACKET_MAP.put(PacketType.Play.Client.ABILITIES, getConstructor(CPacketAbilities.class));
        PACKET_MAP.put(PacketType.Play.Client.ARM_ANIMATION, getConstructor(CPacketArmAnimation.class));
        PACKET_MAP.put(PacketType.Play.Client.BLOCK_DIG, getConstructor(CPacketBlockDig.class));
        PACKET_MAP.put(PacketType.Play.Client.BLOCK_PLACE, getConstructor(CPacketBlockPlace.class));
        PACKET_MAP.put(PacketType.Play.Client.CLIENT_COMMAND, getConstructor(CPacketClientCommand.class));
        PACKET_MAP.put(PacketType.Play.Client.CLOSE_WINDOW, getConstructor(CPacketCloseWindow.class));
        PACKET_MAP.put(PacketType.Play.Client.CUSTOM_PAYLOAD, getConstructor(CPacketCustomPayload.class));
        PACKET_MAP.put(PacketType.Play.Client.ENTITY_ACTION, getConstructor(CPacketEntityAction.class));
        PACKET_MAP.put(PacketType.Play.Client.FLYING, getConstructor(CPacketFlying.class));
        PACKET_MAP.put(PacketType.Play.Client.HELD_ITEM_SLOT, getConstructor(CPacketHeldItemSlot.class));
        PACKET_MAP.put(PacketType.Play.Client.KEEP_ALIVE, getConstructor(CPacketKeepAlive.class));
        PACKET_MAP.put(PacketType.Play.Client.LOOK, getConstructor(CPacketLook.class));
        PACKET_MAP.put(PacketType.Play.Client.POSITION, getConstructor(CPacketPosition.class));
        PACKET_MAP.put(PacketType.Play.Client.POSITION_LOOK, getConstructor(CPacketPositionLook.class));
        PACKET_MAP.put(PacketType.Play.Client.STEER_VEHICLE, getConstructor(CPacketSteerVehicle.class));
        PACKET_MAP.put(PacketType.Play.Client.TRANSACTION, getConstructor(CPacketTransaction.class));
        PACKET_MAP.put(PacketType.Play.Client.USE_ENTITY, getConstructor(CPacketUseEntity.class));
        PACKET_MAP.put(PacketType.Play.Client.WINDOW_CLICK, getConstructor(CPacketWindowClick.class));

        // ===== Server =====
        PACKET_MAP.put(PacketType.Play.Server.ABILITIES, getConstructor(SPacketAbilities.class));
        PACKET_MAP.put(PacketType.Play.Server.BLOCK_CHANGE, getConstructor(SPacketBlockChange.class));
        PACKET_MAP.put(PacketType.Play.Server.CLOSE_WINDOW, getConstructor(SPacketCloseWindow.class));
        PACKET_MAP.put(PacketType.Play.Server.ENTITY, getConstructor(SPacketEntity.class));
        PACKET_MAP.put(PacketType.Play.Server.ENTITY_DESTROY, getConstructor(SPacketEntityDestroy.class));
        PACKET_MAP.put(PacketType.Play.Server.ENTITY_EFFECT, getConstructor(SPacketEntityEffect.class));
        PACKET_MAP.put(PacketType.Play.Server.ENTITY_LOOK, getConstructor(SPacketEntityLook.class));
        PACKET_MAP.put(PacketType.Play.Server.ENTITY_METADATA, getConstructor(SPacketEntityMetadata.class));
        PACKET_MAP.put(PacketType.Play.Server.ENTITY_TELEPORT, getConstructor(SPacketEntityTeleport.class));
        PACKET_MAP.put(PacketType.Play.Server.ENTITY_VELOCITY, getConstructor(SPacketEntityVelocity.class));
        PACKET_MAP.put(PacketType.Play.Server.HELD_ITEM_SLOT, getConstructor(SPacketHeldItemSlot.class));
        PACKET_MAP.put(PacketType.Play.Server.KEEP_ALIVE, getConstructor(SPacketKeepAlive.class));
        PACKET_MAP.put(PacketType.Play.Server.NAMED_ENTITY_SPAWN, getConstructor(SPacketNamedEntitySpawn.class));
        PACKET_MAP.put(PacketType.Play.Server.OPEN_WINDOW, getConstructor(SPacketOpenWindow.class));
        PACKET_MAP.put(PacketType.Play.Server.POSITION, getConstructor(SPacketPosition.class));
        PACKET_MAP.put(PacketType.Play.Server.REL_ENTITY_MOVE, getConstructor(SPacketRelEntityMove.class));
        PACKET_MAP.put(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK, getConstructor(SPacketRelEntityMoveLook.class));
        PACKET_MAP.put(PacketType.Play.Server.REMOVE_ENTITY_EFFECT, getConstructor(SPacketRemoveEntityEffect.class));
        PACKET_MAP.put(PacketType.Play.Server.SPAWN_ENTITY_LIVING, getConstructor(SPacketSpawnEntityLiving.class));
        PACKET_MAP.put(PacketType.Play.Server.TRANSACTION, getConstructor(SPacketTransaction.class));

        // ===== Cache Packets =====
        CACHE_PACKETS.add(PacketType.Play.Server.ENTITY);
        CACHE_PACKETS.add(PacketType.Play.Server.ENTITY_DESTROY);
        CACHE_PACKETS.add(PacketType.Play.Server.ENTITY_LOOK);
        CACHE_PACKETS.add(PacketType.Play.Server.ENTITY_TELEPORT);
        CACHE_PACKETS.add(PacketType.Play.Server.ENTITY_VELOCITY);
        CACHE_PACKETS.add(PacketType.Play.Server.REL_ENTITY_MOVE);
        CACHE_PACKETS.add(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
    }

    private static <T extends WrappedPacket> Constructor<T> getConstructor(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor(PacketContainer.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static PacketType[] getProcessedPackets() {
        return PACKET_MAP.keySet().toArray(new PacketType[0]);
    }

    public static WrappedPacket wrapPacket(PacketType packetType, PacketContainer packetContainer) {
        try {
            return PACKET_MAP.get(packetType).newInstance(packetContainer);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
