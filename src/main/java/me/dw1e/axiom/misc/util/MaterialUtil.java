package me.dw1e.axiom.misc.util;

import org.bukkit.Material;

import java.util.EnumSet;

public final class MaterialUtil {

    public static final EnumSet<Material> SWORDS = EnumSet.of(
            Material.WOOD_SWORD, Material.STONE_SWORD, Material.GOLD_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD
    );

    public static final EnumSet<Material> PISTONS = EnumSet.of(
            Material.PISTON_BASE, Material.PISTON_STICKY_BASE, Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE
    );

}
