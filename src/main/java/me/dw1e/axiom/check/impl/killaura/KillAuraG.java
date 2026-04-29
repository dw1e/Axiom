package me.dw1e.axiom.check.impl.killaura;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.Buffer;
import me.dw1e.axiom.misc.util.MathUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketEntityAction;

import java.util.ArrayDeque;
import java.util.Deque;

public final class KillAuraG extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.KILL_AURA, "G", "检查自动 W-Tap")
                    .setThresholds(8);

    private final Deque<Integer> recentWTapTicks = new ArrayDeque<>();
    private final Buffer buffer = new Buffer(5);

    public KillAuraG(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketEntityAction) {
            CPacketEntityAction wrapper = (CPacketEntityAction) packet;
            EnumWrappers.PlayerAction action = wrapper.getAction();

            int ticksSinceAttack = actionProcessor.getTicksSinceAttack();

            if (action == EnumWrappers.PlayerAction.STOP_SPRINTING
                    && ticksSinceAttack < 10
                    && !actionProcessor.isPlacing()
            ) {
                recentWTapTicks.add(ticksSinceAttack);

                if (recentWTapTicks.size() == 20) {
                    double stdDev = MathUtil.deviation(recentWTapTicks);

                    if (stdDev < 0.3) {
                        if (buffer.add(1.2) >= 2.4) {
                            flag(String.format("STD: %.2f", stdDev));
                        }
                    } else {
                        buffer.reduce(2.0);
                    }

                    recentWTapTicks.clear();
                }
            }
        }
    }

}