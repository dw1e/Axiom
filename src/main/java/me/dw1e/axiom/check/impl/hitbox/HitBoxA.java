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

            entity = entityProcessor.get(wrapper.getEntityId());
            action = wrapper.getAction();

        } else if (packet instanceof CPacketFlying) {
            HitboxEntity entity = this.entity;
            EnumWrappers.EntityUseAction action = this.action;

            this.entity = null;
            this.action = null;

            if (entity == null) return;

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

            double eyeHeight = actionProcessor.wasSneak() ? 1.54 : 1.62;

            Vector eyePos = new Vector(from.getX(), from.getY() + eyeHeight, from.getZ());
            Vector direction = MathUtil.getDirection(to.getYaw(), to.getPitch());

            Ray attackerRay = new Ray(eyePos, direction);
            Vector intersection = box.intersectsRay(attackerRay, 0.0F, Float.MAX_VALUE);

            String actionName = action.name().toLowerCase();
            String entityTypeName = entityType.name().toLowerCase();

            if (intersection != null) {
                double distance = intersection.distance(eyePos);
                double maxDistance = attributeProcessor.isInstantlyBuild() ? 5.0 : 3.0;

                if (distance > maxDistance) {
                    flag(String.format("%s %s at %.5f blocks", actionName, entityTypeName, distance));
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

                            flag(String.format("%s %s through %s", actionName, entityTypeName, blockName));
                            return;
                        }
                    }
                }

            } else {
                flag(String.format("missed hitbox of %s", entityTypeName));
            }
        }
    }
}
