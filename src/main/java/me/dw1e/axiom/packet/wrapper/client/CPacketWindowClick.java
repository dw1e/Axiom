package me.dw1e.axiom.packet.wrapper.client;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import org.bukkit.inventory.ItemStack;

public final class CPacketWindowClick extends WrappedPacket {

    private final int windowId, slot, button, mode;
    private final short actionNumber;
    private final ItemStack clickedItem;
    private ClickType action;

    public CPacketWindowClick(PacketContainer container) {
        StructureModifier<Integer> integers = container.getIntegers();

        windowId = integers.read(0);
        slot = integers.read(1);
        button = integers.read(2);

        actionNumber = container.getShorts().read(0);
        clickedItem = container.getItemModifier().read(0);

        mode = integers.read(3);

        // 借鉴自 me.rhys.anticheat.tinyprotocol.packet.in.WrappedInWindowClickPacket
        if (slot == -1) {
            action = button == 0 ? ClickType.WINDOW_BORDER_LEFT : ClickType.WINDOW_BORDER_RIGHT;
        } else if (mode == 0) {
            if (button == 0) {
                action = ClickType.LEFT;
            } else if (button == 1) {
                action = ClickType.RIGHT;
            }
        } else if (mode == 1) {
            if (button == 0) {
                action = ClickType.SHIFT_LEFT;
            } else if (button == 1) {
                action = ClickType.SHIFT_RIGHT;
            }
        } else if (mode == 2) {
            if (button >= 0 && button < 9) {
                action = ClickType.NUMBER_KEY;
            }
        } else if (mode == 3) {
            if (button == 2) {
                action = ClickType.MIDDLE;
            } else {
                action = ClickType.UNKNOWN;
            }
        } else if (mode == 4) {
            if (slot >= 0) {
                if (button == 0) {
                    action = ClickType.DROP;
                } else if (button == 1) {
                    action = ClickType.CONTROL_DROP;
                }
            } else {
                // 合理的默认设置(因为这种情况发生在他们手中没有任何物品时)
                action = ClickType.LEFT;
                if (button == 1) {
                    action = ClickType.RIGHT;
                }
            }
        } else if (mode == 5) {
            action = ClickType.DRAG;
        } else if (mode == 6) {
            action = ClickType.DOUBLE_CLICK;
        }
    }

    public int getWindowId() {
        return windowId;
    }

    public int getSlot() {
        return slot;
    }

    public int getButton() {
        return button;
    }

    public int getMode() {
        return mode;
    } // 0=单击,1=Shift,2=数字键,3=中键,4=丢弃,5=拖动,6=双击

    public short getActionNumber() {
        return actionNumber;
    }

    public ItemStack getClickedItem() {
        return clickedItem;
    }

    public ClickType getAction() {
        return action;
    }

    public enum ClickType {
        LEFT,
        SHIFT_LEFT,
        RIGHT,
        SHIFT_RIGHT,
        WINDOW_BORDER_LEFT,
        WINDOW_BORDER_RIGHT,
        MIDDLE,
        NUMBER_KEY,
        DOUBLE_CLICK,
        DROP,
        CONTROL_DROP,
        CREATIVE,
        DRAG,
        UNKNOWN;

        public boolean isKeyboardClick() {
            return this == NUMBER_KEY || this == DROP || this == CONTROL_DROP;
        }

        public boolean isCreativeAction() {
            return this == MIDDLE || this == CREATIVE;
        }

        public boolean isRightClick() {
            return this == RIGHT || this == SHIFT_RIGHT;
        }

        public boolean isLeftClick() {
            return this == LEFT || this == SHIFT_LEFT || this == DOUBLE_CLICK || this == CREATIVE;
        }

        public boolean isShiftClick() {
            return this == SHIFT_LEFT || this == SHIFT_RIGHT || this == CONTROL_DROP;
        }
    }
}
