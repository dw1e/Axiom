package me.dw1e.axiom.nms.version;

import me.dw1e.axiom.misc.AABB;
import me.dw1e.axiom.nms.NMSVisitor;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NMS_v1_8_R3 implements NMSVisitor {

    @Override
    public Entity getEntityById(World world, int entityId) {
        WorldServer handle = ((CraftWorld) world).getHandle();
        if (handle == null) return null;

        net.minecraft.server.v1_8_R3.Entity nmsEntity = handle.a(entityId);
        return nmsEntity == null ? null : nmsEntity.getBukkitEntity();
    }

    @Override
    public AABB getEntityBox(Entity entity) {
        CraftEntity craftEntity = (CraftEntity) entity;
        net.minecraft.server.v1_8_R3.Entity nmsEntity = craftEntity.getHandle();
        AxisAlignedBB nmsBox = nmsEntity.getBoundingBox();

        if (nmsBox == null) return AABB.emptyBox(entity.getLocation());

        return new AABB(nmsBox.a, nmsBox.b, nmsBox.c, nmsBox.d, nmsBox.e, nmsBox.f);
    }

    @Override
    public AABB getBlockBox(Block block) {
        CraftWorld craftWorld = (CraftWorld) block.getWorld();
        BlockPosition blockPosition = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData blockData = craftWorld.getHandle().getType(blockPosition);

        net.minecraft.server.v1_8_R3.Block nmsBlock = CraftMagicNumbers.getBlock(block);
        AxisAlignedBB nmsBox = nmsBlock.a(craftWorld.getHandle(), blockPosition, blockData);

        if (nmsBox == null) return AABB.emptyBox(block.getLocation());

        return new AABB(nmsBox.a, nmsBox.b, nmsBox.c, nmsBox.d, nmsBox.e, nmsBox.f);
    }

    @Override
    public List<AABB> getBlockBoxes(Block block) {
        CraftWorld craftWorld = (CraftWorld) block.getWorld();
        net.minecraft.server.v1_8_R3.World world = craftWorld.getHandle();

        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData data = world.getType(pos);
        net.minecraft.server.v1_8_R3.Block nmsBlock = data.getBlock();

        List<AxisAlignedBB> list = new ArrayList<>();

        nmsBlock.a(world, pos, data, AxisAlignedBB.a(
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
        ), list, null);

        if (list.isEmpty()) return Collections.emptyList();

        List<AABB> boxes = new ArrayList<>(list.size());

        for (AxisAlignedBB bb : list) boxes.add(new AABB(bb.a, bb.b, bb.c, bb.d, bb.e, bb.f));

        return boxes;
    }

    @Override
    public Vector getLiquidFlowDirection(Block block) {
        if (block == null || !block.isLiquid()) return new Vector();

        if (!Bukkit.isPrimaryThread()) {
            if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)
                    || !block.getWorld().isChunkLoaded((block.getX() + 1) >> 4, block.getZ() >> 4)
                    || !block.getWorld().isChunkLoaded((block.getX() - 1) >> 4, block.getZ() >> 4)
                    || !block.getWorld().isChunkLoaded(block.getX() >> 4, (block.getZ() + 1) >> 4)
                    || !block.getWorld().isChunkLoaded(block.getX() >> 4, (block.getZ() - 1) >> 4)
            ) return new Vector();
        }

        CraftWorld craftWorld = (CraftWorld) block.getWorld();
        net.minecraft.server.v1_8_R3.World world = craftWorld.getHandle();

        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData data = world.getType(pos);

        net.minecraft.server.v1_8_R3.Block nmsBlock = data.getBlock();

        Vec3D vec = new Vec3D(0, 0, 0);
        vec = nmsBlock.a(world, pos, (net.minecraft.server.v1_8_R3.Entity) null, vec);

        return new Vector(vec.a, vec.b, vec.c);
    }

    @Override
    public float getBlockDigSpeed(Player player, Block block) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        CraftWorld craftWorld = (CraftWorld) block.getWorld();

        EntityHuman entityHuman = craftPlayer.getHandle();
        net.minecraft.server.v1_8_R3.World world = craftWorld.getHandle();

        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData data = world.getType(pos);

        return data.getBlock().getDamage(entityHuman, world, pos);
    }

    @Override
    public void releaseUseItem(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.bV();
    }

    @Override
    public boolean isUsingItem(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        return entityPlayer.bS();
    }
}
