package me.dw1e.axiom.check.impl.netanalysis;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.check.api.handler.TickHandler;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;

public final class NetAnalysisB extends Check implements TickHandler {

    /*
     * Backtrack / LagRange 原理就是在攻击时, 延迟一些时间后, 再响应服务器发送的确认包
     * 这样就可以多摸几下对手的老位置, 从而获得优势
     * 这个检查就是看玩家是否只在攻击时会延迟, 而其它状态下没有
     */

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.NET_ANALYSIS, "B", "检查在战斗中使用回溯")
                    .setVLToAlert(2)
                    .setThresholds(8);

    private int combatSmooth, combatLag;
    private int normalSmooth, normalLag;

    public NetAnalysisB(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
    }

    @Override
    public void onTick() {
        if (SERVER_TICK_TASK.isLagging()) return;

        int ticksSinceAttack = actionProcessor.getTicksSinceAttack();
        boolean hasPingJitter = pingProcessor.hasPingJitter(30L, 0.2);

        /*
         * 战斗窗口: 攻击后 0~2 tick
         * 普通窗口: 攻击后 >=8 tick
         * 3~7 tick 为过渡区, 忽略
         */

        if (ticksSinceAttack <= 2) {
            if (hasPingJitter) {
                ++combatLag;
            } else {
                ++combatSmooth;
            }
        } else if (ticksSinceAttack >= 8) {
            if (hasPingJitter) {
                ++normalLag;
            } else {
                ++normalSmooth;
            }
        }

        int combatTotal = combatLag + combatSmooth;
        int normalTotal = normalLag + normalSmooth;

        // normal 需要更多样本, 避免一直战斗时误判
        if (combatTotal >= 25 && normalTotal >= 50) {

            double combatRatio = (double) combatLag / combatTotal;
            double normalRatio = (double) normalLag / normalTotal;
            double delta = combatRatio - normalRatio;

            if (combatRatio > 0.14 && normalRatio < 0.15 && delta > 0.08) {
                flag(String.format("combat=%.3f, normal=%.3f, delta=%.3f", combatRatio, normalRatio, delta));
            }

            combatSmooth = combatLag = 0;
            normalSmooth = normalLag = 0;
        }
    }
}