package me.dw1e.axiom.packet.wrapper.server;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class SPacketOpenWindow extends WrappedPacket {

    private final int windowId;
    private final String type;
    private final String title;
    private final int slots;
    private final int entityId;

    public SPacketOpenWindow(PacketContainer container) {
        windowId = container.getIntegers().read(0);
        type = container.getStrings().read(0);

        WrappedChatComponent component = container.getChatComponents().read(0);
        title = component != null ? component.getJson() : "";

        slots = container.getIntegers().read(1);

        // 某些版本可能不存在，需要防炸
        entityId = container.getIntegers().size() > 2 ? container.getIntegers().read(2) : -1;
    }

    public int getWindowId() {
        return windowId;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getSlots() {
        return slots;
    }

    public int getEntityId() {
        return entityId;
    }
}