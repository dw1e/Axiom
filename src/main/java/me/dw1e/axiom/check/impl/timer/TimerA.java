package me.dw1e.axiom.check.impl.timer;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.server.SPacketPosition;

public final class TimerA extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.TIMER, "A", "基于时间余额模型检测客户端 Tick 加速")
                    .setThresholds(15);

    private Long lastTimestamp;
    private long balance = -50L;

    public TimerA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying && data.getTick() > 40) {
            long timestamp = System.currentTimeMillis();

            if (lastTimestamp != null) {
                balance += 50L;
                balance -= timestamp - lastTimestamp;

                if (balance > 50L) {
                    flag("balance=" + balance);

                    balance = -50L;
                }
            }

            lastTimestamp = timestamp;
        } else if (packet instanceof SPacketPosition) {
            balance -= 50L;
        }
    }
}
