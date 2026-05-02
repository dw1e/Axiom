package me.dw1e.axiom.check.impl.phase;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.check.api.PunishType;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.AABB;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.server.SPacketPosition;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.EnumSet;

public final class PhaseA extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.PHASE, "A", "检查进入方块的碰撞体积内部")
                    .setThresholds(50)
                    .setPunishType(PunishType.NONE)
                    .setPunishReason("It seems that you are stuck somewhere. Please rejoin the server.");

    // 排除一些会误判的神秘方块
    private static final EnumSet<Material> EXCLUDED_TYPES =
            EnumSet.of(
                    Material.ANVIL
            );

    private PlayerLocation lastGoodLocation;
    private int insideBlockTicks;

    public PhaseA(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            PlayerLocation to = moveProcessor.getTo();

            Block blockIn = getInsideBlock(to);

            insideBlockTicks = blockIn != null ? ++insideBlockTicks : 0;
            int pingTicks = pingProcessor.getPingTicks() + 2;

            if (blockIn != null) {
                if (insideBlockTicks > pingTicks) {
                    Material mat = blockIn.getType();

                    if (!EXCLUDED_TYPES.contains(mat)) {

                        // 上次位置也在墙内, 不检查距离这个范围内 3 格
                        if (lastGoodLocation != null
                                && getInsideBlock(lastGoodLocation) != null
                                && to.distanceSquared(lastGoodLocation) <= 3.0
                        ) return;

                        // 修复观察者模式穿墙时, 如果在墙中切模式会被弹出去
                        if (data.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                            lastGoodLocation = to.clone();
                            violations *= 0.99;
                            return;
                        }

                        if (lastGoodLocation != null) {
                            flag("inside " + mat.name().toLowerCase());

                            moveProcessor.sendTeleport(lastGoodLocation);
                        }
                    }
                }
            } else {
                lastGoodLocation = to.clone();
                violations *= 0.99;
            }

        } else if (packet instanceof SPacketPosition) {
            SPacketPosition wrapper = (SPacketPosition) packet;

            // 强制把传送位置设置为上次ok位置, 不然会出现玩家被传送到较远地方时, 在还未加载出区块时误判, 然后又被拉回去了
            lastGoodLocation = new PlayerLocation(
                    wrapper.getX(), wrapper.getY(), wrapper.getZ(),
                    wrapper.getYaw(), wrapper.getPitch()
            );
        }
    }

    private Block getInsideBlock(PlayerLocation location) {
        AABB playerBox = AABB.PLAYER_COLLISION_BOX.clone();
        playerBox.setMaxY(playerBox.getMaxY() - 1E-6);
        playerBox.shift(location.toVector());

        for (Block block : playerBox.getBlocks(data.getPlayer().getWorld())) {
            if (block.isEmpty() || block.isLiquid()) continue;

            for (AABB blockBox : VISITOR.getBlockBoxes(block)) {
                if (playerBox.intersects(blockBox)) {
                    return block;
                }
            }
        }

        return null;
    }
}