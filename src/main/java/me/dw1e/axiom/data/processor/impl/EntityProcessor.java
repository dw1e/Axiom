package me.dw1e.axiom.data.processor.impl;

import me.dw1e.axiom.data.PlayerData;
import me.dw1e.axiom.data.processor.Processor;
import me.dw1e.axiom.misc.AABB;
import me.dw1e.axiom.misc.HitboxEntity;
import me.dw1e.axiom.misc.util.TaskUtil;
import me.dw1e.axiom.packet.wrapper.WrappedPacket;
import me.dw1e.axiom.packet.wrapper.client.CPacketFlying;
import me.dw1e.axiom.packet.wrapper.server.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EntityProcessor extends Processor {

    private final Map<Integer, HitboxEntity> entityMap = new ConcurrentHashMap<>();

    private boolean onBoat;

    public EntityProcessor(PlayerData data) {
        super(data);

        TaskUtil.runTask(() -> {
            int range = 48; // 48 是 spigot.yml -> entity-tracking-range 里的默认值
            for (Entity entity : data.getPlayer().getNearbyEntities(range, range, range)) {

                int id = entity.getEntityId();
                Location targetLoc = entity.getLocation();

                int x = (int) (targetLoc.getX() * 32.0);
                int y = (int) (targetLoc.getY() * 32.0);
                int z = (int) (targetLoc.getZ() * 32.0);

                putEntityToMap(id, x, y, z);
            }
        });
    }

    @Override
    public void preProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            onBoat = testOnBoat();

        } else if (packet instanceof SPacketEntity) {
            SPacketEntity wrapper = (SPacketEntity) packet;

            HitboxEntity entity = entityMap.get(wrapper.getEntityId());
            if (entity == null) return;

            data.getPingProcessor().confirm(() -> {
                entity.serverPosX += wrapper.getX();
                entity.serverPosY += wrapper.getY();
                entity.serverPosZ += wrapper.getZ();

                double d0 = (double) entity.serverPosX / 32.0;
                double d1 = (double) entity.serverPosY / 32.0;
                double d2 = (double) entity.serverPosZ / 32.0;

                entity.setPositionAndRotation2(d0, d1, d2);
            });

        } else if (packet instanceof SPacketEntityTeleport) {
            SPacketEntityTeleport wrapper = (SPacketEntityTeleport) packet;
            HitboxEntity entity = entityMap.get(wrapper.getEntityId());

            if (entity == null) return;

            data.getPingProcessor().confirm(() -> {
                entity.serverPosX = wrapper.getX();
                entity.serverPosY = wrapper.getY();
                entity.serverPosZ = wrapper.getZ();

                double d0 = (double) entity.serverPosX / 32.0;
                double d1 = (double) entity.serverPosY / 32.0;
                double d2 = (double) entity.serverPosZ / 32.0;

                if (Math.abs(entity.posX - d0) < 0.03125
                        && Math.abs(entity.posY - d1) < 0.015625
                        && Math.abs(entity.posZ - d2) < 0.03125
                ) {
                    entity.setPositionAndRotation2(entity.posX, entity.posY, entity.posZ);
                } else {
                    entity.setPositionAndRotation2(d0, d1, d2);
                }
            });

        } else if (packet instanceof SPacketNamedEntitySpawn) {
            SPacketNamedEntitySpawn wrapper = (SPacketNamedEntitySpawn) packet;

            int entityId = wrapper.getEntityId();

            if (entityId == data.getPlayer().getEntityId()) return;

            if (!entityMap.containsKey(entityId)) {
                putEntityToMap(entityId, wrapper.getX(), wrapper.getY(), wrapper.getZ());
            }

        } else if (packet instanceof SPacketSpawnEntityLiving) {
            SPacketSpawnEntityLiving wrapper = (SPacketSpawnEntityLiving) packet;

            int entityId = wrapper.getEntityId();

            if (!entityMap.containsKey(entityId)) {
                putEntityToMap(entityId, wrapper.getX(), wrapper.getY(), wrapper.getZ());
            }

        } else if (packet instanceof SPacketEntityDestroy) {
            SPacketEntityDestroy wrapper = (SPacketEntityDestroy) packet;

            for (int id : wrapper.getEntities()) {
                entityMap.remove(id);
            }
        }
    }

    @Override
    public void postProcess(WrappedPacket packet) {
        if (packet instanceof CPacketFlying) {
            for (HitboxEntity entity : entityMap.values()) {
                entity.onLivingUpdate();
            }
        }
    }

    public HitboxEntity get(int id) {
        return entityMap.get(id);
    }

    public boolean isOnBoat() {
        return onBoat;
    }

    private void putEntityToMap(int id, int x, int y, int z) {
        World world = data.getPlayer().getWorld();

        TaskUtil.runTask(() -> {
            Entity entity = VISITOR.getEntityById(world, id);
            if (entity == null) return;

            AABB aabb = VISITOR.getEntityBox(entity);
            entityMap.put(id, new HitboxEntity(entity.getType(), x, y, z, aabb.getWidthX(), aabb.getHeight()));
        });
    }

    private boolean testOnBoat() {
        AABB playerBox = AABB.PLAYER_COLLISION_BOX.clone().shift(data.getMoveProcessor().getTo().toVector());

        for (HitboxEntity entity : entityMap.values()) {
            if (entity.getEntityType() != EntityType.BOAT) continue;

            AABB boatBox = entity.getBox().clone().expand(0.5, 0.0, 0.5); // xz + 0.5 修复误判

            return playerBox.getMinY() == boatBox.getMaxY()
                    && playerBox.getMaxX() >= boatBox.getMinX()
                    && playerBox.getMinX() <= boatBox.getMaxX()
                    && playerBox.getMaxZ() >= boatBox.getMinZ()
                    && playerBox.getMinZ() <= boatBox.getMaxZ();
        }

        return false;
    }
}
