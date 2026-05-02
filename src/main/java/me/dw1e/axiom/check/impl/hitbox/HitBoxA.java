package me.dw1e.axiom.check.impl.hitbox;

import com.comphenix.protocol.wrappers.EnumWrappers;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.AABB;
import me.dw1e.axiom.misc.HitboxEntity;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.misc.Ray;
import me.dw1e.axiom.misc.util.MathUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.client.CPacketUseEntity;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.EnumSet;

public final class HitBoxA extends Check {

    // 非常精准且几乎无误判的检测, 不会因为玩家/服务器卡顿而误判, 因为反作弊的视角就是模拟玩家的实时视角
    // 检测强度: Reach 3.005, Hitbox 0.005, 具体取决于你给下面的 HITBOX_EXPAND 设置为多少

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.HITBOX, "A", "检查攻击距离/碰撞箱修改")
                    .setThresholds(8);

    // 由于此检测精度非常高, FastMath 造成的微小 Reach (3.00025 左右) 也会被检测, 所以稍微放宽一点
    private static final double HITBOX_EXPAND = 0.005;

    // 排除会误判的生物
    private static final EnumSet<EntityType> EXCLUDED_TYPES =
            EnumSet.of(
                    EntityType.MINECART, EntityType.BOAT
            );

    private HitboxEntity entity;
    private EnumWrappers.EntityUseAction action;

    public HitBoxA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketUseEntity) {
            CPacketUseEntity wrapper = (CPacketUseEntity) packet;

            // 从实体处理器中获取目标实体 (基于客户端视角模拟的位置数据)
            entity = entityProcessor.get(wrapper.getEntityId());

            // 记录交互类型 (不仅用于攻击检测, 也可用于右键交互距离检测)
            action = wrapper.getAction();
        }
        // 客户端发送数据包顺序: 先发 UseEntity 再发 Flying, 如果在 UseEntity 中直接检测, 此时服务端拿到的是旧位置, 会导致距离计算不准!
        // 不用担心什么发包顺序绕过的问题, BadPacket A 就是检测发包顺序的, 以及他发包顺序不对还会 "误判"
        else if (packet instanceof CPacketFlying) {
            HitboxEntity entity = this.entity;
            EnumWrappers.EntityUseAction action = this.action;

            this.entity = null;
            this.action = null;

            if (entity == null) return;

            // 排除因为误判而不检查的实体类型
            EntityType entityType = entity.getEntityType();
            if (EXCLUDED_TYPES.contains(entityType)) return;

            AABB box = entity.getBox().clone().expand(HITBOX_EXPAND, HITBOX_EXPAND, HITBOX_EXPAND);

            // 1.8 客户端会增加 0.1 的碰撞箱扩展
            box.expand(0.1F, 0.1F, 0.1F);

            // 0.03 偏差
            if (moveProcessor.isOffsetMotion()) box.expand(0.03, 0.03, 0.03);

            // 乘坐载具时不会发送位置更新包, 不检查
            if (actionProcessor.isInsideVehicle()) return;

            PlayerLocation from = moveProcessor.getFrom();
            PlayerLocation to = moveProcessor.getTo();

            // 1.14 以前只有两个高度, 并且这两个高度中间没有衔接
            // 必须用上一次的潜行状态, 因为我们是到下一个位置更新包检查的
            double eyeHeight = actionProcessor.wasSneak() ? 1.54 : 1.62;

            // 客户端在同一 tick 内发送位置更新和攻击包, 但存在发送顺序差异 (先发 UseEntity 再发 Flying)
            // 因此这里使用 from 计算眼睛位置 (对应攻击时的实际位置)
            Vector eyePos = new Vector(from.getX(), from.getY() + eyeHeight, from.getZ());

            // 注意: 视角 (yaw & pitch) 会在下一个位置包中更新
            // 所以这里使用 to 作为最新的视角数据
            Vector direction = MathUtil.getDirection(to.getYaw(), to.getPitch());

            Ray attackerRay = new Ray(eyePos, direction);
            Vector intersection = box.intersectsRay(attackerRay, 0.0F, Float.MAX_VALUE);

            String actionName = action.name().toLowerCase();
            String entityTypeName = entityType.name().toLowerCase();

            if (intersection != null) {
                double distance = intersection.distance(eyePos);

                // 1.8 创造模式实体交互距离手长 5.0 格, 其它模式 3.0 格 (包括观察者模式)
                // PS: 跨版本不会将攻击距离修改, 所以他们只能在创造打出 4.0 格
                boolean isCreative = attributeProcessor.isInstantlyBuild();
                double maxDistance = isCreative ? 5.0 : 3.0;

                // Reach 检查: 与实体交互距离超过限制
                if (distance > maxDistance) {
                    double addVL = Math.min(3.0, Math.floor(distance - maxDistance) + 1.0); // 如果打的远就多加点 VL

                    flag(String.format(
                            "%s %s at %.5f blocks%s",
                            actionName, entityTypeName, distance, (isCreative ? " (creative)" : "")
                    ), addVL);
                }

                BlockIterator iterator = new BlockIterator(data.getPlayer().getWorld(),
                        eyePos, direction, 0.0, (int) distance + 1);

                while (iterator.hasNext()) {
                    Block block = iterator.next();
                    if (block.isEmpty() || block.isLiquid()) continue;

                    for (AABB blockBox : VISITOR.getBlockBoxes(block)) {
                        Vector blockHit = blockBox.intersectsRay(attackerRay, 0.0F, (float) distance);

                        if (blockHit != null && blockHit.distance(eyePos) < distance) {
                            String blockName = block.getType().name().toLowerCase();

                            // 隔墙交互实体检查
                            flag(String.format("%s %s through %s", actionName, entityTypeName, blockName));
                            return;
                        }
                    }
                }

            }
            // Hitbox 检查: 和实体交互了, 但是射线没命中任何实体, 说明他修改了 Hitbox
            else {
                flag(String.format("missed hitbox of %s", entityTypeName));
            }
        }
    }
}
