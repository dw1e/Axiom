package me.dw1e.axiom.misc;

import org.bukkit.entity.EntityType;

public final class HitboxEntity {

    private static final double INV_32 = 1.0 / 32.0;

    private final EntityType entityType;

    private final double width, height;
    private final double halfWidth;

    public int serverPosX, serverPosY, serverPosZ;
    public double posX, posY, posZ;

    private AABB box;
    private int otherPlayerMPPosRotationIncrements;
    private double otherPlayerMPX, otherPlayerMPY, otherPlayerMPZ;

    public HitboxEntity(EntityType entityType, int serverPosX, int serverPosY, int serverPosZ, double width, double height) {
        this.entityType = entityType;

        this.serverPosX = serverPosX;
        this.serverPosY = serverPosY;
        this.serverPosZ = serverPosZ;
        this.width = width;
        this.height = height;
        this.halfWidth = width * 0.5;

        posX = serverPosX * INV_32;
        posY = serverPosY * INV_32;
        posZ = serverPosZ * INV_32;

        double minX = posX - halfWidth;
        double minZ = posZ - halfWidth;

        box = new AABB(minX, posY, minZ, minX + width, posY + height, minZ + width);
    }

    public void onLivingUpdate() {
        if (otherPlayerMPPosRotationIncrements <= 0) return;

        double inv = 1.0 / otherPlayerMPPosRotationIncrements;

        double d0 = posX + (otherPlayerMPX - posX) * inv;
        double d1 = posY + (otherPlayerMPY - posY) * inv;
        double d2 = posZ + (otherPlayerMPZ - posZ) * inv;

        --otherPlayerMPPosRotationIncrements;

        setPosition(d0, d1, d2);
    }

    public void setPositionAndRotation2(double x, double y, double z) {
        otherPlayerMPX = x;
        otherPlayerMPY = y;
        otherPlayerMPZ = z;

        otherPlayerMPPosRotationIncrements = 3;
    }

    private void setPosition(double x, double y, double z) {
        posX = x;
        posY = y;
        posZ = z;

        double minX = x - halfWidth;
        double minZ = z - halfWidth;

        box = new AABB(minX, y, minZ, minX + width, y + height, minZ + width);
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public AABB getBox() {
        return box;
    }
}