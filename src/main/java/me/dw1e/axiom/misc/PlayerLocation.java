package me.dw1e.axiom.misc;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class PlayerLocation implements Cloneable {

    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;
    private boolean updatePos, updateRot;

    public PlayerLocation(double x, double y, double z, float yaw, float pitch,
                          boolean onGround, boolean updatePos, boolean updateRot) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.updatePos = updatePos;
        this.updateRot = updateRot;
    }

    public PlayerLocation(double x, double y, double z, float yaw, float pitch) {
        this(x, y, z, yaw, pitch, false, true, true);
    }

    public PlayerLocation add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public double distanceSquared(PlayerLocation other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;

        return dx * dx + dy * dy + dz * dz;
    }

    public boolean matches(PlayerLocation other) {
        return Double.compare(x, other.x) == 0
                && Double.compare(y, other.y) == 0
                && Double.compare(z, other.z) == 0
                && Float.compare(yaw, other.yaw) == 0
                && Float.compare(pitch, other.pitch) == 0;
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public PlayerLocation clone() {
        try {
            return (PlayerLocation) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PlayerLocation)) return false;

        PlayerLocation other = (PlayerLocation) obj;

        return matches(other)
                && onGround == other.onGround
                && updatePos == other.updatePos
                && updateRot == other.updateRot;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean isUpdatePos() {
        return updatePos;
    }

    public void setUpdatePos(boolean updatePos) {
        this.updatePos = updatePos;
    }

    public boolean isUpdateRot() {
        return updateRot;
    }

    public void setUpdateRot(boolean updateRot) {
        this.updateRot = updateRot;
    }
}