package me.dw1e.axiom.misc.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.misc.AABB;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.*;

public final class BlockUtil {

    public static Block getBlockAsync(Location loc) {
        if (loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
            return loc.getBlock();
        } else {
            return null;
        }
    }

    public static float getSlipperiness(Material material) {
        switch (material) {
            case SLIME_BLOCK:
                return 0.8F;
            case ICE:
            case PACKED_ICE:
                return 0.98F;
            default:
                return 0.6F;
        }
    }

    public static AABB getFullBlockAABB(BlockPosition blockPos) {
        return new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1);
    }

    @SuppressWarnings("deprecation")
    public static void resyncBlockAt(Player player, Block block) {
        if (player == null || block == null) return;

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);

        packet.getBlockPositionModifier().write(0, new BlockPosition(block.getLocation().toVector()));
        packet.getBlockData().write(0, WrappedBlockData.createData(block.getType(), block.getData()));

        Axiom.getPlugin().getProtocolManager().sendServerPacket(player, packet);
    }

    public static void resyncBlockAt(Player player, BlockPosition blockPosition) {
        Block block = getBlockAsync(blockPosition.toLocation(player.getWorld()));

        if (block != null) resyncBlockAt(player, block);
    }

    public static boolean isReallySolid(Block block) {
        boolean reallySolid = block.getType().isSolid();

        MaterialData matData = block.getState().getData();

        if (matData instanceof Sign || matData instanceof Banner) {
            reallySolid = false;

        } else if (matData instanceof FlowerPot
                || matData instanceof Diode
                || matData instanceof Skull
                || matData instanceof Ladder
                || block.getType() == Material.CARPET
                || block.getType() == Material.REDSTONE_COMPARATOR
                || block.getType() == Material.REDSTONE_COMPARATOR_ON
                || block.getType() == Material.REDSTONE_COMPARATOR_OFF
                || block.getType() == Material.SOIL
                || block.getType() == Material.WATER_LILY
                || block.getType() == Material.SNOW
                || block.getType() == Material.COCOA
        ) {
            reallySolid = true;
        }

        return reallySolid;
    }

}
