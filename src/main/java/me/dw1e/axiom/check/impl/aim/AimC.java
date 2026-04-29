package me.dw1e.axiom.check.impl.aim;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.check.api.PunishType;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.misc.evicting.EvictingList;
import me.dw1e.axiom.misc.util.MathUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.client.CPacketUseEntity;
import org.bukkit.util.Vector;

// 借鉴自 Hawk 作者 Islandscout
public final class AimC extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.AIM, "C", "启发式瞄准检查")
                    .setVLToAlert(5)
                    .setThresholds(30)
                    .setPunishType(PunishType.KICK) // PS: 此检测不适合自动封禁, 误判较多, 仅适合辅助判定玩家是否作弊!
                    .setPunishReason("Suspicious aiming behavior detected.");

    private final EvictingList<Vector> mouseMoves = new EvictingList<>(5);
    private final EvictingList<Long> clickTimes = new EvictingList<>(5);

    public AimC(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            PlayerLocation from = moveProcessor.getFrom();
            PlayerLocation to = moveProcessor.getTo();

            processMove(from, to);

        } else if ((packet instanceof CPacketUseEntity
                && ((CPacketUseEntity) packet).getAction() == EnumWrappers.EntityUseAction.ATTACK)
                || (packet instanceof CPacketBlockPlace
                && ((CPacketBlockPlace) packet).isPlacedBlock())) {
            processClick();
        }
    }

    private void processMove(PlayerLocation from, PlayerLocation to) {
        float deltaYaw = MathUtil.wrapAngleTo180(to.getYaw() - from.getYaw());
        float deltaPitch = to.getPitch() - from.getPitch();

        Vector mouseMove = new Vector(deltaYaw, deltaPitch, 0);
        mouseMoves.add(mouseMove);

        if (clickedXMovesBefore()) {
            double minSpeed = Double.MAX_VALUE;
            double maxSpeed = 0.0;
            double maxAngle = 0.0;

            for (int i = 1; i < mouseMoves.size(); i++) {
                Vector lastMouseMove = mouseMoves.get(i - 1);
                Vector currMouseMove = mouseMoves.get(i);

                double speed = currMouseMove.length();
                double lastSpeed = lastMouseMove.length();
                double angle = lastSpeed != 0.0 ? MathUtil.angle(lastMouseMove, currMouseMove) : 0.0;

                if (Double.isNaN(angle)) angle = 0.0;

                minSpeed = Math.min(speed, minSpeed);
                maxSpeed = Math.max(speed, maxSpeed);
                maxAngle = Math.max(angle, maxAngle);

                if (maxSpeed - minSpeed > 4.0 && minSpeed < 0.01 && maxAngle < 0.1 && lastSpeed > 1.0) { // 断断续续
                    flag(String.format(
                            "A: max=%.3f, min=%.3f, angle=%.3f, last=%.3f",
                            maxSpeed, minSpeed, maxAngle, lastSpeed
                    ));

                } else if (speed > 20.0 && lastSpeed > 20.0 && angle > 2.86) { // 抽搐 / 之字形
                    flag(String.format(
                            "B: speed=%.3f, last=%.3f, angle=%.3f",
                            speed, lastSpeed, angle
                    ));

                } else if (speed - lastSpeed < -30.0 && angle > 0.8) { // 跳跃式不连续
                    flag(String.format(
                            "C: speed=%.3f, last=%.3f, angle=%.3f",
                            speed, lastSpeed, angle
                    ));

                } else {
                    violations *= 0.99;
                }
            }
        }
    }

    private void processClick() {
        long currTick = data.getTick();
        if (!clickTimes.contains(currTick)) clickTimes.add(currTick);
    }

    private boolean clickedXMovesBefore() {
        long time = data.getTick() - 2;

        for (int i = 0; i < clickTimes.size(); i++) {
            if (time == clickTimes.get(i)) {
                clickTimes.remove(i);
                return true;
            }
        }

        return false;
    }
}
