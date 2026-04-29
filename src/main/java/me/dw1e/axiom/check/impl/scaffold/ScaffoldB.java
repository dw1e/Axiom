package me.dw1e.axiom.check.impl.scaffold;

import com.comphenix.protocol.wrappers.BlockPosition;
import me.dw1e.axiom.check.Check;
import me.dw1e.axiom.check.CheckMeta;
import me.dw1e.axiom.check.api.Category;
import me.dw1e.axiom.check.api.PunishType;
import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.misc.PlayerLocation;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketBlockPlace;
import org.bukkit.block.BlockFace;

public final class ScaffoldB extends Check {

    private static final CheckMeta CHECK_META =
            new CheckMeta(Category.SCAFFOLD, "B", "检查在方块边缘的安全行走")
                    .setVLToAlert(3)
                    .setThresholds(15)
                    .setPunishType(PunishType.KICK);

    private BlockPosition lastBlockPos;

    public ScaffoldB(PlayerData data) {
        super(data, CHECK_META);
    }

    @Override
    public void handle(WrappedPacket packet) {
        if (packet instanceof CPacketBlockPlace) {
            CPacketBlockPlace wrapper = (CPacketBlockPlace) packet;
            if (!wrapper.isPlacedBlock()) return;

            BlockFace face = wrapper.getBlockFace();
            if (face == BlockFace.UP || face == BlockFace.DOWN) return;

            BlockPosition blockPos = wrapper.getBlockPosition().add(new BlockPosition(wrapper.getBlockFaceNormal()));

            if (lastBlockPos == null) {
                lastBlockPos = blockPos;
                return;
            }

            int blockX = blockPos.getX();
            int blockY = blockPos.getY();
            int blockZ = blockPos.getZ();

            int lastX = lastBlockPos.getX();
            int lastY = lastBlockPos.getY();
            int lastZ = lastBlockPos.getZ();

            int dx = blockX - lastX;
            int dz = blockZ - lastZ;

            int ticksSinceSneak = actionProcessor.getTicksSinceSneak();

            boolean invalidStep = blockY != lastY || (blockX == lastX && blockZ == lastZ);
            boolean notAdjacent = Math.abs(dx) + Math.abs(dz) != 1;

            double delta = moveProcessor.getTo().getY() - blockY;
            boolean underfoot = delta >= 1.0 && delta <= 2.25;

            if (ticksSinceSneak < 10 || invalidStep || notAdjacent || !underfoot) {
                violations *= 0.99;
                lastBlockPos = blockPos;
                return;
            }

            double edgeDist = getEdgeDist(dx, dz);

            if (edgeDist < 0.035) {
                flag(String.format("dist=%.3f, sneak=%s", edgeDist, (ticksSinceSneak < 40 ? ticksSinceSneak : "false")));

            } else if (edgeDist >= 0.07) {
                violations *= 0.99;
            }

            lastBlockPos = blockPos;
        }
    }

    private double getEdgeDist(int dx, int dz) {
        PlayerLocation to = moveProcessor.getTo();

        double fracX = to.getX() - Math.floor(to.getX());
        double fracZ = to.getZ() - Math.floor(to.getZ());

        double edgeDist;

        if (dx != 0.0) {
            edgeDist = dx > 0.0 ? Math.abs(fracX - 0.3) : Math.abs(fracX - 0.7);
        } else {
            edgeDist = dz > 0.0 ? Math.abs(fracZ - 0.3) : Math.abs(fracZ - 0.7);
        }

        return edgeDist;
    }

}