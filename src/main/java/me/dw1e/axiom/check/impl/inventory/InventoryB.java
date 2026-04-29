package me.dw1e.axiom.check.impl.inventory;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class InventoryB extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.INVENTORY, "B", "检查在背包开启时移动视角")
                    .setThresholds(5);

    public InventoryB(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying
                && ((CPacketFlying) packet).isRotation()
                && !actionProcessor.isInsideVehicle()
                && moveProcessor.getTicksSinceTeleport() > 2
        ) {
            int openTicks = actionProcessor.getInventoryOpenTicks();
            if (openTicks > 0) { // 玩家可以在背包开启的第一刻移动视角

                flag("openTicks=" + openTicks);

                actionProcessor.closeInventory();
            }
        }
    }

}
