package me.dw1e.axiom.check.impl.inventory;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class InventoryA extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.INVENTORY, "A", "检查在背包开启时 潜行/疾跑/使用物品")
                    .setThresholds(5);

    public InventoryA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying
                && ((CPacketFlying) packet).isPosition()
                && actionProcessor.isInventoryOpen()
                && !actionProcessor.isInsideVehicle()
        ) {
            boolean flagged = false;

            if (actionProcessor.isSneaking()) {
                flag("sneaking");
                flagged = true;
            }

            if (actionProcessor.isSprinting()) {
                flag("sprinting");
                flagged = true;
            }

            if (actionProcessor.isUsingItem()) {
                flag("usingItem");
                flagged = true;
            }

            if (flagged) actionProcessor.closeInventory();
        }
    }

}
