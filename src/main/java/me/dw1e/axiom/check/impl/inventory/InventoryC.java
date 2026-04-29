package me.dw1e.axiom.check.impl.inventory;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.util.TaskUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketWindowClick;
import org.bukkit.Material;

public final class InventoryC extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.INVENTORY, "C", "检查同时点击多个背包槽位")
                    .setThresholds(5);

    private int lastClickedTicks;
    private int lastSlot;
    private Material lastClickedItem;

    public InventoryC(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketWindowClick) {
            CPacketWindowClick wrapper = (CPacketWindowClick) packet;

            CPacketWindowClick.ClickType clickType = wrapper.getAction();
            Material clickedItem = wrapper.getClickedItem().getType();

            // 排除掉 Shift + 双击 可以一键把同类型的物品快速转移的情况
            if ((clickType.isShiftClick() || clickType == CPacketWindowClick.ClickType.DRAG)
                    && (clickedItem == lastClickedItem || clickedItem == Material.AIR)) return;

            int clickedTicks = data.getTick();
            int delta = clickedTicks - lastClickedTicks;

            int slot = wrapper.getSlot();

            if (delta == 0 && slot != lastSlot) {
                if (flag("clickType=" + clickType)) {
                    packet.setCancel(true);
                    TaskUtil.runTask(() -> data.getPlayer().updateInventory());
                }
            }

            lastClickedTicks = clickedTicks;
            lastSlot = slot;
            lastClickedItem = clickedItem;
        }
    }

}
