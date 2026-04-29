package me.dw1e.axiom.check.impl.scaffold;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketArmAnimation;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;

public final class ScaffoldA extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.SCAFFOLD, "A", "检查在放置方块时不挥手")
                    .setThresholds(3);

    private boolean swung, placed;
    private boolean pendingViolation;
    private int pendingTicks;

    public ScaffoldA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketArmAnimation) {
            swung = true;

        } else if (packet instanceof CPacketBlockPlace && ((CPacketBlockPlace) packet).isPlacedBlock()) {
            placed = true;

        } else if (packet instanceof CPacketFlying) {
            if (pendingViolation && ++pendingTicks > 1) {
                pendingViolation = false;
            }

            // 由于只要玩家右键就会发 BlockPlace 包, 会存在发了包但没放方块的情况
            // 所以需要经过 Bukkit 的 BlockPlace 事件确认一下是否真的放置了方块

            if (placed && !swung) {
                pendingViolation = true;
                pendingTicks = 0;
            }

            swung = placed = false;
        }
    }

    public void confirmPlace() {
        if (pendingViolation) flag();

        pendingViolation = false;
    }
}