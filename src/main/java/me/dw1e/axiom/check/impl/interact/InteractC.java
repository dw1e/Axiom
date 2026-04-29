package me.dw1e.axiom.check.impl.interact;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;

public final class InteractC extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.INTERACT, "C", "检查无效的方块位置向量")
                    .setThresholds(1);

    public InteractC(PlayerData data) {
        super(data, CHECK_META);
    }

    private static boolean invalid(float v) {
        return v < 0.0F || v > 1.0F;
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketBlockPlace) {
            CPacketBlockPlace wrapper = (CPacketBlockPlace) packet;

            int face = wrapper.getFace();

            if (face == 6) flag("invalid face");

            float x = wrapper.getFacingX(), y = wrapper.getFacingY(), z = wrapper.getFacingZ();

            if (face != 255 && (invalid(x) || invalid(y) || invalid(z))) {
                flag("face=" + face + ", x=" + x + ", y=" + y + ", z=" + z);
            }
        }
    }
}