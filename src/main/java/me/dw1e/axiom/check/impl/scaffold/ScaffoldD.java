package me.dw1e.axiom.check.impl.scaffold;

import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.check.api.PunishType;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public final class ScaffoldD extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.SCAFFOLD, "D", "检查跳跃搭路 (Same Y)")
                    .setEnabled(false)
                    .setVLToAlert(3)
                    .setThresholds(15)
                    .setPunishType(PunishType.KICK);

    private Location lastBlockPos;

    public ScaffoldD(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
    }

    public void check(Block block) {
        Location blockPos = block.getLocation();

        if (lastBlockPos == null) {
            lastBlockPos = blockPos;
            return;
        }

        int blockX = blockPos.getBlockX();
        int blockY = blockPos.getBlockY();
        int blockZ = blockPos.getBlockZ();

        int lastX = lastBlockPos.getBlockX();
        int lastY = lastBlockPos.getBlockY();
        int lastZ = lastBlockPos.getBlockZ();

        int airTicks = moveProcessor.getAirTicks();

        int dx = blockX - lastX;
        int dz = blockZ - lastZ;

        boolean bridging = blockPos.getBlock().getRelative(BlockFace.DOWN).isEmpty();
        boolean sameY = blockY == lastY;
        boolean noMove = blockX == lastX && blockZ == lastZ;
        boolean notAdjacent = Math.abs(dx) + Math.abs(dz) != 1;

        double delta = moveProcessor.getTo().getY() - blockY;
        boolean underfoot = delta >= 1.0 && delta < 2.25;

        if (airTicks < 2 || !bridging || !sameY || noMove || notAdjacent || !underfoot) {
            violations *= 0.99;
            lastBlockPos = blockPos;
            return;
        }

        flag("airTicks=" + airTicks);

        lastBlockPos = blockPos;
    }

}