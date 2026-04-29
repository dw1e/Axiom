package me.dw1e.axiom.misc;

import me.dw1e.axiom.Axiom;
import me.dw1e.axiom.misc.util.BlockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

// 部分借鉴自 Hawk 作者 Islandscout
public final class AABB implements Cloneable {

    public static final AABB PLAYER_COLLISION_BOX = new AABB(-0.3, 0, -0.3, 0.3, 1.8, 0.3);
    public static final AABB WATER_COLLISION_BOX = new AABB(-0.299, 0.401, -0.299, 0.299, 1.399, 0.299);
    public static final AABB LAVA_COLLISION_BOX = new AABB(-0.2, 0.4, -0.2, 0.2, 1.4, 0.2);

    private double minX, minY, minZ, maxX, maxY, maxZ;

    public AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public static AABB emptyBox(Location center) {
        return new AABB(center.getX(), center.getY(), center.getZ(), center.getX(), center.getY(), center.getZ());
    }

    public AABB shift(Vector vector) {
        minX += vector.getX();
        minY += vector.getY();
        minZ += vector.getZ();
        maxX += vector.getX();
        maxY += vector.getY();
        maxZ += vector.getZ();
        return this;
    }

    public AABB expand(double x, double y, double z) {
        minX -= x;
        minY -= y;
        minZ -= z;
        maxX += x;
        maxY += y;
        maxZ += z;
        return this;
    }

    public Vector intersectsRay(Ray ray, float minDist, float maxDist) {
        Vector invDir = new Vector(
                1.0F / ray.getDirection().getX(),
                1.0F / ray.getDirection().getY(),
                1.0F / ray.getDirection().getZ()
        );

        boolean signDirX = invDir.getX() < 0.0;
        boolean signDirY = invDir.getY() < 0.0;
        boolean signDirZ = invDir.getZ() < 0.0;

        Vector max = new Vector(maxX, maxY, maxZ);
        Vector min = new Vector(minX, minY, minZ);

        Vector bbox = signDirX ? max : min;
        double tmin = (bbox.getX() - ray.getOrigin().getX()) * invDir.getX();
        bbox = signDirX ? min : max;
        double tmax = (bbox.getX() - ray.getOrigin().getX()) * invDir.getX();
        bbox = signDirY ? max : min;
        double tyMin = (bbox.getY() - ray.getOrigin().getY()) * invDir.getY();
        bbox = signDirY ? min : max;
        double tyMax = (bbox.getY() - ray.getOrigin().getY()) * invDir.getY();

        if ((tmin > tyMax) || (tyMin > tmax)) return null;

        if (tyMin > tmin) tmin = tyMin;
        if (tyMax < tmax) tmax = tyMax;

        bbox = signDirZ ? max : min;
        double tzMin = (bbox.getZ() - ray.getOrigin().getZ()) * invDir.getZ();
        bbox = signDirZ ? min : max;
        double tzMax = (bbox.getZ() - ray.getOrigin().getZ()) * invDir.getZ();

        if ((tmin > tzMax) || (tzMin > tmax)) return null;

        if (tzMin > tmin) tmin = tzMin;
        if (tzMax < tmax) tmax = tzMax;

        if ((tmin < maxDist) && (tmax > minDist)) return ray.getPointAtDistance(tmin);

        return null;
    }

    public List<Block> getBlocks(World world) {
        List<Block> blocks = new ArrayList<>();

        for (int x = (int) Math.floor(minX); x < (int) Math.ceil(maxX); x++) {
            for (int y = (int) Math.floor(minY); y < (int) Math.ceil(maxY); y++) {
                for (int z = (int) Math.floor(minZ); z < (int) Math.ceil(maxZ); z++) {

                    Block block = BlockUtil.getBlockAsync(new Location(world, x, y, z));
                    if (block != null) blocks.add(block);
                }
            }
        }

        return blocks;
    }

    public List<AABB> getBlockAABBs(World world, Material... exemptedMats) {
        EnumSet<Material> exempt = exemptedMats.length == 0
                ? EnumSet.noneOf(Material.class) : EnumSet.copyOf(Arrays.asList(exemptedMats));

        List<AABB> aabbs = new ArrayList<>();

        AABB expanded = clone();
        expanded.setMinY(expanded.getMinY() - 1);

        for (Block block : expanded.getBlocks(world)) {
            if (exempt.contains(block.getType())) continue;

            List<AABB> boxes = Axiom.getPlugin().getNmsManager().getVisitor().getBlockBoxes(block);
            for (AABB box : boxes) {
                if (isColliding(box)) {
                    aabbs.add(box);
                }
            }
        }

        return aabbs;
    }

    public EnumSet<Material> getMaterials(World world) {
        EnumSet<Material> materials = EnumSet.noneOf(Material.class);

        for (int x = (int) Math.floor(minX); x < (int) Math.ceil(maxX); x++) {
            for (int y = (int) Math.floor(minY); y < (int) Math.ceil(maxY); y++) {
                for (int z = (int) Math.floor(minZ); z < (int) Math.ceil(maxZ); z++) {
                    Block block = BlockUtil.getBlockAsync(new Location(world, x, y, z));
                    if (block != null) materials.add(block.getType());
                }
            }
        }

        return materials;
    }

    public boolean isColliding(AABB other) {
        return maxX >= other.minX && minX <= other.maxX
                && maxY >= other.minY && minY <= other.maxY
                && maxZ >= other.minZ && minZ <= other.maxZ;
    }

    public boolean containsPoint(Vector point) {
        return point.getX() >= minX && point.getX() <= maxX
                && point.getY() >= minY && point.getY() <= maxY
                && point.getZ() >= minZ && point.getZ() <= maxZ;
    }

    public boolean intersectsX(AABB other) {
        return maxX > other.minX && minX < other.maxX;
    }

    public boolean intersectsY(AABB other) {
        return maxY > other.minY && minY < other.maxY;
    }

    public boolean intersectsZ(AABB other) {
        return maxZ > other.minZ && minZ < other.maxZ;
    }

    public boolean intersectsXZ(AABB other) {
        return intersectsX(other) && intersectsZ(other);
    }

    public boolean intersects(AABB other) {
        return intersectsXZ(other) && intersectsY(other);
    }

    public AABB clone() {
        try {
            return (AABB) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public double getWidthX() {
        return maxX - minX;
    }

    public double getWidthZ() {
        return maxZ - minZ;
    }

    public double getHeight() {
        return maxY - minY;
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public double getMinZ() {
        return minZ;
    }

    public void setMinZ(double minZ) {
        this.minZ = minZ;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(double maxZ) {
        this.maxZ = maxZ;
    }
}
