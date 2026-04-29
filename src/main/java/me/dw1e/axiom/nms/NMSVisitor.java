package me.dw1e.axiom.nms;

import me.dw1e.axiom.misc.AABB;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public interface NMSVisitor {

    Entity getEntityById(World world, int entityId);

    AABB getEntityBox(Entity entity);

    AABB getBlockBox(Block block);

    List<AABB> getBlockBoxes(Block block);

    Vector getLiquidFlowDirection(Block block);

    float getBlockDigSpeed(Player player, Block block);

    void releaseUseItem(Player player);

    boolean isUsingItem(Player player);
}
