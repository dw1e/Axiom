package me.dw1e.axiom.check.impl.fly;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.AdjacentBlocks;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.misc.util.BlockUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import org.bukkit.World;
import org.bukkit.block.Block;

// 借鉴自 Hawk 作者 Islandscout
public final class FlyB extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.FLY, "B", "检查地面状态欺骗")
                    .setVLToAlert(3)
                    .setThresholds(30);

    public FlyB(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            PlayerLocation from = moveProcessor.getFrom();
            PlayerLocation to = moveProcessor.getTo();

            CPacketFlying wrapper = (CPacketFlying) packet;
            World world = data.getPlayer().getWorld();

            boolean onGroundReally;

            if (wrapper.isPosition()) {
                onGroundReally = collideProcessor.isOnGroundReally();
            } else {
                onGroundReally = AdjacentBlocks.onGroundReally(moveProcessor.getLastPosLoc().toLocation(world),
                        moveProcessor.getLastPosDeltaY(), true, 0.001);
            }

            if (!collideProcessor.isStep() && !onGroundReally) {
                if (wrapper.isOnGround()) {
                    PlayerLocation checkLoc = from.clone();
                    checkLoc.setY(to.getY());

                    boolean invalid = !AdjacentBlocks.onGroundReally(
                            checkLoc.toLocation(world), -1, false, 0.02
                    );

                    boolean exempt = data.getTick() < 20
                            || entityProcessor.isOnBoat()
                            || actionProcessor.isInsideVehicle()
                            || collideProcessor.isOnGhostBlock()
                            || collideProcessor.isInWeb()
                            || collideProcessor.getTicksSincePushedByPiston() < 2;

                    if (invalid && !exempt) {

                        if (flag()) {
                            if (violations >= 5.0) moveProcessor.sendTeleport(moveProcessor.getLastGroundLoc());

                            Block underFeet = moveProcessor.getTo().clone()
                                    .add(0, -1, 0).toLocation(world).getBlock();

                            if (underFeet != null) BlockUtil.resyncBlockAt(data.getPlayer(), underFeet);
                        }
                    }

                } else {
                    violations *= 0.995;
                }
            }
        }
    }
}
