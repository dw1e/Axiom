package me.dw1e.axiom.check.impl.scaffold;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import org.bukkit.block.BlockFace;

public final class ScaffoldE extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.SCAFFOLD, "E", "检查异常的放置成功率")
                    .setThresholds(10);

    private int success, failure;
    private int pendingPlaces;

    public ScaffoldE(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketBlockPlace) {
            CPacketBlockPlace wrapper = (CPacketBlockPlace) packet;

            BlockFace face = wrapper.getBlockFace();
            if (face == BlockFace.DOWN || face == BlockFace.UP) return;

            if (isExempt()) return;

            if (wrapper.isPlacedBlock()) {
                pendingPlaces++;
            } else {
                failure++;
            }

            check();
        }
    }

    public void confirmPlace() {
        if (isExempt()) return;

        if (pendingPlaces > 0) {
            pendingPlaces--;

            success++;
        } else {
            failure++;
        }

        check();
    }

    private boolean isExempt() {
        return !collideProcessor.isOnEdge()
                || actionProcessor.isSneaking()
                || attributeProcessor.isFlying()
                || moveProcessor.getDeltaXZ() < attributeProcessor.getAttributeSpeed();
    }

    private void check() {
        int total = success + failure;

        if (total >= 10) {
            double deltaXZ = moveProcessor.getDeltaXZ();

            double ratio = (double) success / total;
            double limit = deltaXZ < 0.2 ? 0.9 : 0.6;

            if (ratio >= limit) flag(String.format("ratio=%.2f, dxz=%.3f", ratio, deltaXZ));

            success = failure = 0;
        }
    }
}