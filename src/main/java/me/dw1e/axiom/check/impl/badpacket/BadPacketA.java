package me.dw1e.axiom.check.impl.badpacket;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.Buffer;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class BadPacketA extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.BAD_PACKET, "A", "检查无效的数据包顺序 (Post)")
                    .setThresholds(10);

    private static final Set<Class<? extends WrappedPacket>> TRIGGER_PACKETS =
            new HashSet<>(Arrays.asList(
                    CPacketArmAnimation.class,
                    CPacketBlockDig.class,
                    CPacketBlockPlace.class,
                    CPacketCustomPayload.class,
                    CPacketEntityAction.class,
                    CPacketHeldItemSlot.class,
                    CPacketUseEntity.class,
                    CPacketWindowClick.class
            ));

    private final Buffer buffer = new Buffer(5);

    private long lastFlying, lastTriggerPacket;
    private boolean sentFlying;

    private String lastTriggerPacketType = "";

    public BadPacketA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        long now = System.currentTimeMillis();

        if (packet instanceof CPacketFlying) {
            long delay = now - lastTriggerPacket;

            if (sentFlying) {
                if (delay > 40L && delay < 100L) {
                    if (buffer.add() > 2) {
                        flag("delay=" + delay + ", packet=" + lastTriggerPacketType);
                    }
                } else {
                    buffer.reduce(0.1);
                }
                sentFlying = false;
            }

            lastFlying = now;
            return;
        }

        Class<?> clazz = packet.getClass();

        if (TRIGGER_PACKETS.contains(clazz)) {
            long delay = now - lastFlying;

            if (delay < 10L) {
                lastTriggerPacket = now;
                lastTriggerPacketType = clazz.getSimpleName().substring(7);

                sentFlying = true;
            } else {
                buffer.reduce(0.1);
            }
        }
    }
}
