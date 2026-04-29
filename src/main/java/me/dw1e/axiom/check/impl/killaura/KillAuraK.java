package me.dw1e.axiom.check.impl.killaura;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import org.bukkit.util.Vector;

public final class KillAuraK extends Check {

    // 检测例如开 KillAura 不移动鼠标, 在敌人离开索敌范围后, 视角移回原准星位置

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.KILL_AURA, "K", "检查 KillAura 的视角移动特征")
                    .setThresholds(1);

    private Vector lastNoRotationCursorPos;
    private int stage;

    public KillAuraK(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            PlayerLocation to = moveProcessor.getTo();

            Vector cursorPos = new Vector(to.getYaw(), to.getPitch(), 0);

            // 记录第一次未移动的视角
            if (stage == 0 && !to.isUpdateRot()) {
                stage = 1;
                lastNoRotationCursorPos = cursorPos;
            }
            // 视角开始移动, 且必须有一定的移动幅度
            else if (stage == 1 && to.isUpdateRot()
                    && moveProcessor.getDeltaYaw() > 1.0F
                    && moveProcessor.getDeltaPitch() > 1.0F) {
                stage = 2;
            }
            // 停止移动后, 视角又回到了最初的位置
            else if (stage == 2 && !to.isUpdateRot()) {
                double delta = lastNoRotationCursorPos.distance(cursorPos);

                // 防止TP误判
                if (delta == 0.0 && moveProcessor.getTicksSinceTeleport() > 3) flag();

                stage = 0;
            }

        }
    }
}
